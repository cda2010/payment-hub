package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.casting.CatalogParticipantDefinition;
import com.gateway.payment.catalog.casting.CatalogProductCasting;
import com.gateway.payment.catalog.casting.CatalogProductCastingService;
import com.gateway.payment.catalog.casting.CatalogUiBindingResolver;
import com.gateway.payment.datasources.DataSourceResolveService;
import com.gateway.payment.catalog.service.CatalogProductResolver;
import com.gateway.payment.common.domain.NextActionType;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.i18n.FormI18nLocalizer;
import com.gateway.payment.common.i18n.GatewayI18nService;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOtpConfig;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOtpConfigRepository;
import com.gateway.payment.payment.api.dto.FormFieldDto;
import com.gateway.payment.payment.api.dto.NextActionDto;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Construit {@link NextActionDto} depuis la userTask Flowable active + formKey
 * (sans {@code payment_flow_step}). Résout aussi les bindings catalogue (P2).
 */
@Service
public class BpmCheckoutNextActionService {

    private static final Map<String, String> PAY_PROVIDER_ICONS = Map.of(
            "ORANGE_MONEY", "phone-portrait-outline",
            "WAVE", "water-outline",
            "CORIS_MONEY", "wallet-outline",
            "MOOV_MONEY", "phone-portrait-outline",
            "FREE_MONEY", "phone-portrait-outline",
            "CARD_PAYMENT", "card-outline",
            "CORIS_BANK", "business-outline"
    );

    private final PaymentProcessSupport processSupport;
    private final CheckoutFormCatalog formCatalog;
    private final FormI18nLocalizer formI18nLocalizer;
    private final GatewayI18nService i18nService;
    private final PaymentProviderRepository providerRepository;
    private final ProviderOtpConfigRepository otpConfigRepository;
    private final ObjectMapper objectMapper;
    private final CatalogUiBindingResolver uiBindingResolver;
    private final CatalogProductResolver catalogProductResolver;
    private final CatalogProductCastingService castingService;
    private final DataSourceResolveService dataSourceResolveService;

    public BpmCheckoutNextActionService(
            PaymentProcessSupport processSupport,
            CheckoutFormCatalog formCatalog,
            FormI18nLocalizer formI18nLocalizer,
            GatewayI18nService i18nService,
            PaymentProviderRepository providerRepository,
            ProviderOtpConfigRepository otpConfigRepository,
            ObjectMapper objectMapper,
            CatalogUiBindingResolver uiBindingResolver,
            CatalogProductResolver catalogProductResolver,
            CatalogProductCastingService castingService,
            DataSourceResolveService dataSourceResolveService
    ) {
        this.processSupport = processSupport;
        this.formCatalog = formCatalog;
        this.formI18nLocalizer = formI18nLocalizer;
        this.i18nService = i18nService;
        this.providerRepository = providerRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.objectMapper = objectMapper;
        this.uiBindingResolver = uiBindingResolver;
        this.catalogProductResolver = catalogProductResolver;
        this.castingService = castingService;
        this.dataSourceResolveService = dataSourceResolveService;
    }

    public NextActionDto resolve(PaymentTransaction tx) {
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return NextActionDto.builder().type(NextActionType.SUCCESS).message("Paiement réussi").build();
        }
        if (tx.getStatus() == PaymentStatus.FAILED
                || tx.getStatus() == PaymentStatus.CANCELLED
                || tx.getStatus() == PaymentStatus.EXPIRED) {
            return NextActionDto.builder()
                    .type(NextActionType.FAILURE)
                    .message(tx.getErrorMessage() != null ? tx.getErrorMessage() : "Paiement échoué")
                    .build();
        }
        if (tx.getStatus() == PaymentStatus.WAITING_APPROVAL) {
            return waitAction(tx, "Transfert en attente d'approbation");
        }
        if (tx.getStatus() == PaymentStatus.RETURNED_FOR_CORRECTION) {
            // Formulaire de reprise si tâche active
            Task t = processSupport.findActiveTask(tx.getPaymentId(), null);
            if (t != null && isReturnTask(t.getFormKey(), t.getTaskDefinitionKey())) {
                return formAction(tx, t.getFormKey() != null ? t.getFormKey() : "account-transfer-return");
            }
            return waitAction(tx, tx.getErrorMessage() != null
                    ? tx.getErrorMessage()
                    : "Approbation refusée — correction requise");
        }
        if (tx.getStatus() == PaymentStatus.PENDING
                || tx.getStatus() == PaymentStatus.UNKNOWN
                || tx.getStatus() == PaymentStatus.WAITING_CALLBACK
                || tx.getStatus() == PaymentStatus.PROCESSING) {
            return waitAction(tx);
        }
        if (tx.getStatus() == PaymentStatus.REDIRECT_REQUIRED) {
            Object url = readContext(tx).get("redirectUrl");
            if (url != null) {
                return NextActionDto.builder()
                        .type(NextActionType.REDIRECT)
                        .url(String.valueOf(url))
                        .message("Vous allez être redirigé vers la page de paiement sécurisée")
                        .build();
            }
            return waitAction(tx);
        }

        Task task = processSupport.findActiveTask(tx.getPaymentId(), null);
        if (task == null) {
            if (!processSupport.isAlive(tx.getPaymentId()) && tx.getStatus() == PaymentStatus.SUCCESS) {
                return NextActionDto.builder().type(NextActionType.SUCCESS).message("Paiement réussi").build();
            }
            return waitAction(tx);
        }

        String bpmnFormKey = task.getFormKey();
        String taskKey = task.getTaskDefinitionKey();
        Map<String, Object> ctx = readContext(tx);
        CatalogUiBindingResolver.ResolvedUi resolved =
                uiBindingResolver.resolve(ctx, taskKey, bpmnFormKey);
        String formKey = resolved.formKey() != null ? resolved.formKey() : bpmnFormKey;

        if (isOtpTask(formKey, taskKey)) {
            return otpAction(tx);
        }
        if (isApprovalTask(formKey, taskKey)) {
            // L'initiateur poll en WAIT ; les validateurs passent par APPROVE/REJECT.
            return waitAction(tx, "Transfert en attente d'approbation");
        }
        if (isWaitTask(formKey, taskKey)) {
            Object url = ctx.get("redirectUrl");
            if (url != null && (tx.getStatus() == PaymentStatus.REDIRECT_REQUIRED
                    || tx.getStatus() == PaymentStatus.AWAITING_USER_INPUT
                    || tx.getStatus() == PaymentStatus.WAITING_CALLBACK)) {
                return NextActionDto.builder()
                        .type(NextActionType.REDIRECT)
                        .url(String.valueOf(url))
                        .message("Vous allez être redirigé vers la page de paiement sécurisée")
                        .build();
            }
            return waitAction(tx);
        }
        if (resolved.hasInlineSchema()) {
            return formActionInline(tx, resolved, ctx);
        }
        return formAction(tx, formKey);
    }

    private NextActionDto formActionInline(
            PaymentTransaction tx,
            CatalogUiBindingResolver.ResolvedUi resolved,
            Map<String, Object> ctx
    ) {
        Map<String, Object> formSchema = resolved.formSchema();
        if (formSchema != null) {
            formSchema = hydrateRfDataSources(formSchema, tx);
            formSchema = filterCollected(formSchema, tx);
        }
        List<FormFieldDto> fields = formSchema != null
                ? fieldsFromFormio(formSchema)
                : (resolved.fields() != null ? resolved.fields() : List.of());
        // Retirer aussi les FormFieldDto déjà collectés (lookup au start hub),
        // sauf sélections (select avec options) — ex. liste de factures.
        if (fields != null && !fields.isEmpty()) {
            fields = fields.stream()
                    .filter(f -> f != null && f.getName() != null
                            && (hasSelectOptions(f) || !isAlreadyCollected(f.getName(), tx)))
                    .toList();
        }
        String message = null;
        if (ctx.get("bills") instanceof List<?> bills && !bills.isEmpty()) {
            message = bills.size() + " facture(s) trouvée(s) — sélectionnez celle à payer.";
        } else if (ctx.get("billLabel") != null) {
            message = "Facture trouvée : " + ctx.get("billLabel")
                    + " — Montant dû : " + tx.getAmount() + " " + tx.getCurrency()
                    + (ctx.get("customerName") != null ? " (" + ctx.get("customerName") + ")" : "")
                    + ". Cochez la case pour confirmer, puis continuez.";
        }
        Map<String, Object> screenData = null;
        if (ctx.get("bills") instanceof List<?> bills && !bills.isEmpty()) {
            screenData = new LinkedHashMap<>();
            screenData.put("bills", bills);
            if (ctx.get("billCount") != null) {
                screenData.put("billCount", ctx.get("billCount"));
            }
        }
        return NextActionDto.builder()
                .type(NextActionType.FORM)
                .formKey(resolved.formKey())
                .fields(fields)
                .formSchema(formSchema)
                .message(message)
                .data(screenData)
                .build();
    }

    public boolean isWaitTask(String formKey, String taskDefinitionKey) {
        Optional<String> declared = formCatalog.gatewayNextAction(formKey);
        if (declared.filter(a -> "WAIT".equalsIgnoreCase(a) || "REDIRECT".equalsIgnoreCase(a)).isPresent()) {
            return true;
        }
        String tk = taskDefinitionKey != null ? taskDefinitionKey.toLowerCase() : "";
        return tk.contains("wait") || tk.contains("callback");
    }

    public boolean isOtpTask(String formKey, String taskDefinitionKey) {
        Optional<String> declared = formCatalog.gatewayNextAction(formKey);
        if (declared.filter(a -> "ENTER_OTP".equalsIgnoreCase(a)).isPresent()) {
            return true;
        }
        String tk = taskDefinitionKey != null ? taskDefinitionKey.toLowerCase() : "";
        if (tk.contains("enterotp") || "enterotp".equals(tk) || tk.endsWith("otp") && tk.contains("enter")) {
            return true;
        }
        String fk = formKey != null ? formKey.toLowerCase() : "";
        // formKey explicite OTP (pas "provider-otp-phone")
        return fk.endsWith("-otp")
                || fk.endsWith("-otp-code")
                || fk.contains("otp-code")
                || fk.contains("enter-otp")
                || "otp".equals(fk);
    }

    public boolean isApprovalTask(String formKey, String taskDefinitionKey) {
        Optional<String> declared = formCatalog.gatewayNextAction(formKey);
        if (declared.filter(a -> "APPROVAL".equalsIgnoreCase(a)).isPresent()) {
            return true;
        }
        String tk = taskDefinitionKey != null ? taskDefinitionKey.toUpperCase() : "";
        if (tk.startsWith("APPROVAL") || tk.contains("WAITAPPROVAL") || "WAIT_APPROVAL".equals(tk)) {
            return true;
        }
        String fk = formKey != null ? formKey.toLowerCase() : "";
        return fk.contains("approval") || fk.endsWith("-approve");
    }

    public boolean isReturnTask(String formKey, String taskDefinitionKey) {
        String tk = taskDefinitionKey != null ? taskDefinitionKey.toUpperCase() : "";
        if (tk.contains("CORRECT") || tk.contains("RETURN")) {
            return true;
        }
        String fk = formKey != null ? formKey.toLowerCase() : "";
        return fk.contains("return") || fk.contains("correct");
    }

    private NextActionDto formAction(PaymentTransaction tx, String formKey) {
        String lang = i18nService.currentLanguage();
        Map<String, Object> formSchema = formCatalog.open(formKey)
                .map(body -> formI18nLocalizer.localizeFormBody(body, lang))
                .map(body -> {
                    Object formio = body.get("formio");
                    if (formio instanceof Map<?, ?> map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> schema = (Map<String, Object>) map;
                        schema = enrichPayProviderOptions(schema, formKey, tx);
                        schema = hydrateRfDataSources(schema, tx);
                        return filterCollected(schema, tx);
                    }
                    return null;
                })
                .orElseGet(() -> formCatalog.formioSchema(formKey)
                        .map(schema -> formI18nLocalizer.localizeFormioSchema(schema, lang))
                        .map(schema -> enrichPayProviderOptions(schema, formKey, tx))
                        .map(schema -> hydrateRfDataSources(schema, tx))
                        .map(schema -> filterCollected(schema, tx))
                        .orElse(null));
        List<FormFieldDto> fields = formSchema != null
                ? fieldsFromFormio(formSchema)
                : List.of();
        Map<String, Object> ctx = readContext(tx);
        String message = null;
        if ("provider-bill-select-pay".equals(formKey)) {
            message = "Choisissez le moyen de paiement pour acquitter la facture"
                    + (tx.getAmount() != null ? " (" + tx.getAmount() + " " + tx.getCurrency() + ")" : "")
                    + ".";
        } else if (ctx.get("billLabel") != null) {
            message = "Facture trouvée : " + ctx.get("billLabel")
                    + " — Montant dû : " + tx.getAmount() + " " + tx.getCurrency()
                    + (ctx.get("customerName") != null ? " (" + ctx.get("customerName") + ")" : "")
                    + ". Cochez la case pour confirmer, puis continuez.";
        }
        return NextActionDto.builder()
                .type(NextActionType.FORM)
                .formKey(formKey)
                .fields(fields)
                .formSchema(formSchema)
                .message(message)
                .build();
    }

    /**
     * Hydrate les selects déclarant {@code properties.rfDataSource}
     * (LOCAL ou OMNICANAL via {@link DataSourceResolveService}).
     */
    private Map<String, Object> hydrateRfDataSources(Map<String, Object> schema, PaymentTransaction tx) {
        if (schema == null || dataSourceResolveService == null) {
            return schema;
        }
        String productCode = null;
        Map<String, Object> ctx = readContext(tx);
        Object raw = ctx.get("catalogServiceCode");
        if (raw != null && !String.valueOf(raw).isBlank()) {
            productCode = String.valueOf(raw).trim();
        }
        return dataSourceResolveService.hydrateFormSchema(schema, productCode);
    }

    /**
     * Remplit le select {@code payProvider} avec les wallets PAYEMENT actifs,
     * restreints aux participants PAYER du casting produit (comme ACCOUNT_TRANSFER
     * pour les rails), hors rail facture. Chaque option porte {@code icon}/{@code sub}.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> enrichPayProviderOptions(
            Map<String, Object> schema,
            String formKey,
            PaymentTransaction tx
    ) {
        if (schema == null || !"provider-bill-select-pay".equals(formKey)) {
            return schema;
        }
        Map<String, Object> copy = objectMapper.convertValue(schema, new TypeReference<>() {});
        Object components = copy.get("components");
        if (!(components instanceof List<?> list)) {
            return copy;
        }
        Map<String, Object> ctx = readContext(tx);
        String billCode = null;
        Object billCtx = ctx.get("billProviderCode");
        if (billCtx != null) {
            billCode = String.valueOf(billCtx).trim();
        }
        if (billCode == null || billCode.isBlank()) {
            billCode = tx.getProviderCode();
        }
        Set<String> allowed = payerProviderCodes(ctx);
        List<Map<String, Object>> values = new ArrayList<>();
        Set<String> seen = new java.util.LinkedHashSet<>();
        for (PaymentProvider p : providerRepository.findActiveByServiceType(ServiceTypeCode.PAYEMENT)) {
            if (p.getParticipantKind() == FseParticipantKind.FACTURIER) {
                continue;
            }
            if (billCode != null && billCode.equalsIgnoreCase(p.getCode())) {
                continue;
            }
            String code = p.getCode() != null ? p.getCode().trim().toUpperCase(Locale.ROOT) : "";
            if (!allowed.isEmpty() && !allowed.contains(code)) {
                continue;
            }
            values.add(payProviderOption(p, code));
            seen.add(code);
        }
        // Casting PAYER prime : un participant déclaré (ex. CORIS_BANK) doit
        // apparaître même s'il n'était pas encore typé PAYEMENT au listing.
        for (String code : allowed) {
            if (seen.contains(code) || (billCode != null && billCode.equalsIgnoreCase(code))) {
                continue;
            }
            providerRepository.findByCode(code).ifPresent(p -> {
                if (p.isActif() && p.getParticipantKind() != FseParticipantKind.FACTURIER) {
                    values.add(payProviderOption(p, code));
                    seen.add(code);
                }
            });
        }
        List<Object> updated = new ArrayList<>();
        for (Object c : list) {
            if (!(c instanceof Map<?, ?> comp)) {
                updated.add(c);
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>((Map<String, Object>) comp);
            if ("payProvider".equals(String.valueOf(m.get("key")))) {
                Object data = m.get("data");
                Map<String, Object> dataMap = data instanceof Map<?, ?> dm
                        ? new LinkedHashMap<>((Map<String, Object>) dm)
                        : new HashMap<>();
                dataMap.put("values", values);
                m.put("data", dataMap);
                m.put("dataSrc", "values");
            }
            updated.add(m);
        }
        copy.put("components", updated);
        return copy;
    }

    /** Codes providers déclarés au casting pour le rôle PAYER (vide = pas de filtre). */
    private Set<String> payerProviderCodes(Map<String, Object> ctx) {
        Object rawCode = ctx.get("catalogServiceCode");
        if (rawCode == null || String.valueOf(rawCode).isBlank()) {
            return Set.of();
        }
        if (catalogProductResolver == null || castingService == null) {
            return Set.of();
        }
        try {
            var catalog = catalogProductResolver.resolve(String.valueOf(rawCode).trim(), null);
            if (catalog == null || catalog.configuration() == null) {
                return Set.of();
            }
            CatalogProductCasting casting = castingService.parse(catalog.configuration());
            return casting.participants().stream()
                    .filter(p -> "PAYER".equalsIgnoreCase(p.role()))
                    .map(CatalogParticipantDefinition::providerCode)
                    .filter(c -> c != null && !c.isBlank())
                    .map(c -> c.trim().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        } catch (RuntimeException ex) {
            return Set.of();
        }
    }

    private static String payProviderSub(String code) {
        if (code.contains("CARD")) {
            return "Carte";
        }
        if (code.contains("BANK")) {
            return "Compte bancaire";
        }
        return "Wallet mobile";
    }

    private Map<String, Object> payProviderOption(PaymentProvider p, String code) {
        Map<String, Object> opt = new LinkedHashMap<>();
        opt.put("label", p.getLibelle() != null ? p.getLibelle() : p.getCode());
        opt.put("value", p.getCode());
        opt.put("icon", PAY_PROVIDER_ICONS.getOrDefault(code, "wallet-outline"));
        opt.put("sub", payProviderSub(code));
        return opt;
    }

    private NextActionDto otpAction(PaymentTransaction tx) {
        var otpConfig = providerRepository.findByCode(tx.getProviderCode())
                .flatMap(p -> otpConfigRepository.findByProviderId(p.getId()));
        Integer expires = otpConfig.map(ProviderOtpConfig::getValiditySeconds).orElse(120);
        Integer length = otpConfig.map(ProviderOtpConfig::getLength).orElse(6);
        return NextActionDto.builder()
                .type(NextActionType.ENTER_OTP)
                .expiresIn(expires)
                .otpLength(length)
                .message("Saisissez le code OTP reçu (" + length + " chiffres)")
                .build();
    }

    private NextActionDto waitAction(PaymentTransaction tx) {
        return waitAction(tx, null);
    }

    private NextActionDto waitAction(PaymentTransaction tx, String defaultMessage) {
        String message = defaultMessage != null ? defaultMessage : "Paiement en cours de traitement";
        if (tx != null) {
            Object custom = readContext(tx).get("waitMessage");
            if (custom != null && !String.valueOf(custom).isBlank()) {
                message = String.valueOf(custom);
            }
        }
        return NextActionDto.builder()
                .type(NextActionType.WAIT)
                .pollAfterSeconds(3)
                .message(message)
                .build();
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> filterCollected(Map<String, Object> schema, PaymentTransaction tx) {
        try {
            Map<String, Object> copy = objectMapper.convertValue(schema, new TypeReference<>() {});
            Object components = copy.get("components");
            if (!(components instanceof List<?> list)) {
                return copy;
            }
            List<Object> filtered = new ArrayList<>();
            for (Object c : list) {
                if (!(c instanceof Map<?, ?> comp)) {
                    filtered.add(c);
                    continue;
                }
                // Toujours conserver les sélections (liste factures, payProvider, …)
                if (isChoiceWithOptions(comp)) {
                    filtered.add(c);
                    continue;
                }
                String key = comp.get("key") != null ? String.valueOf(comp.get("key")) : null;
                if (key != null && isAlreadyCollected(key, tx)) {
                    continue;
                }
                filtered.add(c);
            }
            copy.put("components", filtered);
            return copy;
        } catch (Exception e) {
            return schema;
        }
    }

    private boolean isAlreadyCollected(String name, PaymentTransaction tx) {
        Map<String, Object> ctx = readContext(tx);
        boolean hasBillList = ctx.get("bills") instanceof List<?> bills && !bills.isEmpty();
        // Étape sélection facture : ne pas masquer les champs du formulaire selectBill.
        if (hasBillList && ("billReference".equals(name) || "amount".equalsIgnoreCase(name)
                || "billLabel".equals(name))) {
            return false;
        }
        // Un montant différé (facture, CAPTURE_AMOUNT) naît à zéro, dans la transaction
        // comme dans le contexte : le repli générique ci-dessous le prendrait pour une
        // saisie faite, viderait l'étape chargée de le collecter et laisserait filer un
        // montant nul. La transaction fait foi, sans repli sur le contexte.
        if ("amount".equalsIgnoreCase(name)) {
            return tx.getAmount() != null && tx.getAmount().compareTo(BigDecimal.ZERO) > 0;
        }
        if ("phoneNumber".equalsIgnoreCase(name) && tx.getPhoneNumber() != null && !tx.getPhoneNumber().isBlank()) {
            return true;
        }
        return ctx.containsKey(name) && ctx.get(name) != null;
    }

    private static boolean hasSelectOptions(FormFieldDto field) {
        return field.getOptions() != null && !field.getOptions().isEmpty();
    }

    private static boolean isChoiceWithOptions(Map<?, ?> comp) {
        String type = comp.get("type") != null ? String.valueOf(comp.get("type")) : "";
        if (!"select".equalsIgnoreCase(type)
                && !"radio".equalsIgnoreCase(type)
                && !"selectboxes".equalsIgnoreCase(type)) {
            return false;
        }
        Object values = comp.get("values");
        if (values == null && comp.get("data") instanceof Map<?, ?> data) {
            values = data.get("values");
        }
        return values instanceof List<?> list && !list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<FormFieldDto> fieldsFromFormio(Map<String, Object> formSchema) {
        Object components = formSchema.get("components");
        if (!(components instanceof List<?> list)) {
            return List.of();
        }
        List<FormFieldDto> fields = new ArrayList<>();
        for (Object c : list) {
            if (!(c instanceof Map<?, ?> raw)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> comp = (Map<String, Object>) raw;
            String type = comp.get("type") != null ? String.valueOf(comp.get("type")) : "";
            if ("button".equalsIgnoreCase(type)) {
                continue;
            }
            String key = comp.get("key") != null ? String.valueOf(comp.get("key")) : null;
            if (key == null) {
                continue;
            }
            String label = comp.get("label") != null ? String.valueOf(comp.get("label")) : key;
            boolean required = false;
            Object validate = comp.get("validate");
            if (validate instanceof Map<?, ?> v && Boolean.TRUE.equals(v.get("required"))) {
                required = true;
            }
            String fieldType = switch (type.toLowerCase()) {
                case "phonenumber" -> "tel";
                case "number" -> "number";
                case "password" -> "password";
                case "textarea" -> "textarea";
                case "select", "radio", "selectboxes" -> "select";
                case "checkbox" -> "checkbox";
                case "email" -> "email";
                default -> "text";
            };
            List<Map<String, String>> options = formioSelectOptions(comp);
            fields.add(FormFieldDto.builder()
                    .name(key)
                    .label(label)
                    .type(fieldType)
                    .required(required)
                    .options(options.isEmpty() ? null : options)
                    .build());
        }
        return fields;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> formioSelectOptions(Map<String, Object> comp) {
        List<Map<String, String>> out = new ArrayList<>();
        Object values = comp.get("values");
        if (values == null && comp.get("data") instanceof Map<?, ?> data) {
            values = data.get("values");
        }
        if (!(values instanceof List<?> list)) {
            return out;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Object value = m.get("value");
                Object label = m.get("label");
                if (value == null && label == null) {
                    continue;
                }
                String v = String.valueOf(value != null ? value : label);
                String l = String.valueOf(label != null ? label : value);
                Map<String, String> opt = new LinkedHashMap<>();
                opt.put("label", l);
                opt.put("value", v);
                Object icon = m.get("icon");
                if (icon != null && !String.valueOf(icon).isBlank()) {
                    opt.put("icon", String.valueOf(icon));
                }
                Object sub = m.get("sub");
                if (sub != null && !String.valueOf(sub).isBlank()) {
                    opt.put("sub", String.valueOf(sub));
                }
                out.add(opt);
            } else if (item != null) {
                String v = String.valueOf(item);
                Map<String, String> opt = new LinkedHashMap<>();
                opt.put("label", v);
                opt.put("value", v);
                out.add(opt);
            }
        }
        return out;
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
