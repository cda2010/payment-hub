package com.gateway.payment.catalog.casting;

import com.gateway.payment.billing.api.dto.ProductFieldDto;
import com.gateway.payment.billing.domain.BillerProduct;
import com.gateway.payment.billing.service.BillerService;
import com.gateway.payment.billing.service.ProductUiSchemaService;
import com.gateway.payment.catalog.document.CatalogDocumentUploadSchemaBuilder;
import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.catalog.recap.CatalogRecapSchemaBuilder;
import com.gateway.payment.payment.api.dto.FormFieldDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Résout l'UI d'une userTask via bindings catalogue (P2),
 * y compris {@link CatalogFormSource#BILLER_PRODUCT} et la nomenclature Form.io.
 */
@Service
public class CatalogUiBindingResolver {

    private final CatalogProductCastingService castingService;
    private final BillerService billerService;
    private final ProductUiSchemaService uiSchemaService;
    private final CatalogRecapSchemaBuilder recapSchemaBuilder;
    private final CatalogDocumentUploadSchemaBuilder documentSchemaBuilder;
    private final CatalogConfigurationLocalizer configurationLocalizer;

    public CatalogUiBindingResolver(
            CatalogProductCastingService castingService,
            BillerService billerService,
            ProductUiSchemaService uiSchemaService,
            CatalogRecapSchemaBuilder recapSchemaBuilder,
            CatalogDocumentUploadSchemaBuilder documentSchemaBuilder,
            CatalogConfigurationLocalizer configurationLocalizer
    ) {
        this.castingService = castingService;
        this.billerService = billerService;
        this.uiSchemaService = uiSchemaService;
        this.recapSchemaBuilder = recapSchemaBuilder;
        this.documentSchemaBuilder = documentSchemaBuilder;
        this.configurationLocalizer = configurationLocalizer;
    }

    @SuppressWarnings("unchecked")
    public ResolvedUi resolve(Map<String, Object> context, String activityId, String bpmnFormKey) {
        Map<String, Object> configuration = configurationFromContext(context);
        CatalogProductCasting casting = castingService.parse(configuration);
        Map<String, String> roleAssignments = readRoleAssignments(context);

        String assignedForLookup = null;
        Optional<CatalogBindingDefinition> bindingOpt = casting.findBinding(activityId, null);
        if (bindingOpt.isPresent() && bindingOpt.get().actorRole() != null) {
            assignedForLookup = roleAssignments.get(bindingOpt.get().actorRole());
        }
        CatalogBindingDefinition binding = casting.findBinding(activityId, assignedForLookup).orElse(null);

        if (binding == null || binding.kind() != CatalogBindingKind.UI) {
            if (documentSchemaBuilder.shouldRenderDocuments(null, bpmnFormKey, activityId, context)) {
                return resolveDocumentsUi(context, null);
            }
            if (recapSchemaBuilder.shouldRenderRecap(null, bpmnFormKey, activityId, configuration)) {
                return resolveRecapUi(context, null);
            }
            return ResolvedUi.formKey(bpmnFormKey, null, null, FormKeyResolution.BPMN);
        }

        if (binding.formSource() == CatalogFormSource.BILLER_PRODUCT) {
            return resolveBillerProductUi(context, binding);
        }

        if (documentSchemaBuilder.shouldRenderDocuments(
                binding.formSource(), binding.formKey(), activityId, context)) {
            return resolveDocumentsUi(context, binding.actorRole());
        }

        if (recapSchemaBuilder.shouldRenderRecap(
                binding.formSource(), binding.formKey(), activityId, configuration)) {
            return resolveRecapUi(context, binding.actorRole());
        }

        ResolvedUi formKeyUi = resolveFormKeyUi(context, binding, assignedForLookup, bpmnFormKey);
        if ("selectBill".equals(activityId) || "provider-bill-select".equals(formKeyUi.formKey())) {
            return enrichSelectBillForm(context, formKeyUi);
        }
        return formKeyUi;
    }

    /** Enrichit le formulaire de sélection avec les options issues de {@code context.bills}. */
    private ResolvedUi enrichSelectBillForm(Map<String, Object> context, ResolvedUi base) {
        Object billsRaw = context.get("bills");
        if (!(billsRaw instanceof List<?> bills) || bills.isEmpty()) {
            return base;
        }
        List<Map<String, Object>> values = new ArrayList<>();
        for (Object item : bills) {
            if (!(item instanceof Map<?, ?> m)) {
                continue;
            }
            Object ref = firstNonNull(m.get("reference"), m.get("billReference"), m.get("id"));
            if (ref == null) {
                continue;
            }
            Object label = firstNonNull(m.get("libelle"), m.get("billLabel"), m.get("label"), m.get("customerName"));
            Object amount = firstNonNull(m.get("amountDue"), m.get("montant"), m.get("amount"));
            Object currency = firstNonNull(m.get("currency"));
            String display = String.valueOf(ref);
            if (label != null && !String.valueOf(label).equals(display)) {
                display = display + " — " + label;
            }
            if (amount != null) {
                display = display + " — " + amount;
                if (currency != null) {
                    display = display + " " + currency;
                }
            }
            values.add(Map.of("label", display, "value", String.valueOf(ref)));
        }
        if (values.isEmpty()) {
            return base;
        }
        Map<String, Object> schema = base.formSchema() != null
                ? new LinkedHashMap<>(base.formSchema())
                : new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components = schema.get("components") instanceof List<?> list
                ? (List<Map<String, Object>>) (List<?>) new ArrayList<>(list)
                : new ArrayList<>();
        Map<String, Object> select = new LinkedHashMap<>();
        select.put("type", "select");
        select.put("key", "billReference");
        select.put("label", "Facture");
        select.put("input", true);
        select.put("dataSrc", "values");
        select.put("data", Map.of("values", values));
        select.put("widget", "choicesjs");
        select.put("validate", Map.of("required", true));
        List<Map<String, Object>> rebuilt = new ArrayList<>();
        rebuilt.add(select);
        for (Map<String, Object> c : components) {
            if (c != null && !"billReference".equals(c.get("key"))) {
                rebuilt.add(c);
            }
        }
        schema.put("display", "form");
        schema.put("components", rebuilt);
        return new ResolvedUi(
                base.formKey(),
                base.fields(),
                schema,
                base.actorRole(),
                base.formSource(),
                base.billerScreen(),
                base.formKeyResolution()
        );
    }

    private static Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object v : values) {
            if (v != null && !String.valueOf(v).isBlank()) {
                return v;
            }
        }
        return null;
    }

    private ResolvedUi resolveDocumentsUi(Map<String, Object> context, String actorRole) {
        Map<String, Object> schema = documentSchemaBuilder.build(context);
        return new ResolvedUi(
                CatalogDocumentUploadSchemaBuilder.FORM_KEY,
                null,
                schema,
                actorRole,
                CatalogFormSource.DOCUMENTS,
                null,
                FormKeyResolution.SHARED_BINDING
        );
    }

    private ResolvedUi resolveRecapUi(Map<String, Object> context, String actorRole) {
        Map<String, Object> configuration = configurationLocalizer.localize(
                versionIdFromContext(context), configurationFromContext(context));
        Map<String, Object> schema = recapSchemaBuilder.build(configuration, context);
        return new ResolvedUi(
                CatalogRecapSchemaBuilder.FORM_KEY,
                null,
                schema,
                actorRole,
                CatalogFormSource.RECAP,
                null,
                FormKeyResolution.SHARED_BINDING
        );
    }

    /**
     * FORM_KEY :
     * <ol>
     *   <li>Override explicite (binding.providerCode + formKey)</li>
     *   <li>Convention {@code product-role-provider} si formKey binding vide</li>
     *   <li>formKey partagé du binding</li>
     *   <li>formKey BPMN</li>
     * </ol>
     */
    private ResolvedUi resolveFormKeyUi(
            Map<String, Object> context,
            CatalogBindingDefinition binding,
            String assignedProvider,
            String bpmnFormKey
    ) {
        boolean explicitProviderOverride = StringUtils.hasText(binding.providerCode())
                && StringUtils.hasText(binding.formKey());
        if (explicitProviderOverride) {
            return ResolvedUi.formKey(
                    binding.formKey(),
                    binding.actorRole(),
                    CatalogFormSource.FORM_KEY,
                    FormKeyResolution.EXPLICIT_PROVIDER
            );
        }

        String productCode = string(context, "catalogServiceCode");
        boolean formKeyBlank = !StringUtils.hasText(binding.formKey());
        if (formKeyBlank && StringUtils.hasText(binding.actorRole()) && StringUtils.hasText(assignedProvider)) {
            String conventional = CatalogFormKeyNomenclature.conventionalFormKey(
                    productCode, binding.actorRole(), assignedProvider);
            if (StringUtils.hasText(conventional)) {
                return ResolvedUi.formKey(
                        conventional,
                        binding.actorRole(),
                        CatalogFormSource.FORM_KEY,
                        FormKeyResolution.CONVENTION
                );
            }
        }

        // formKey renseigné = écran partagé pour tous les providers du rôle
        if (StringUtils.hasText(binding.formKey())) {
            return ResolvedUi.formKey(
                    binding.formKey(),
                    binding.actorRole(),
                    CatalogFormSource.FORM_KEY,
                    FormKeyResolution.SHARED_BINDING
            );
        }

        // formKey vide mais convention incomplète → dernier recours BPMN
        if (StringUtils.hasText(binding.actorRole()) && StringUtils.hasText(assignedProvider)) {
            String conventional = CatalogFormKeyNomenclature.conventionalFormKey(
                    productCode, binding.actorRole(), assignedProvider);
            if (StringUtils.hasText(conventional)) {
                return ResolvedUi.formKey(
                        conventional,
                        binding.actorRole(),
                        CatalogFormSource.FORM_KEY,
                        FormKeyResolution.CONVENTION
                );
            }
        }

        return ResolvedUi.formKey(
                bpmnFormKey,
                binding.actorRole(),
                CatalogFormSource.FORM_KEY,
                FormKeyResolution.BPMN
        );
    }

    public Optional<CatalogBindingDefinition> findInvokeBinding(
            Map<String, Object> context,
            String activityId
    ) {
        Map<String, Object> configuration = configurationFromContext(context);
        CatalogProductCasting casting = castingService.parse(configuration);
        Map<String, String> roleAssignments = readRoleAssignments(context);
        Optional<CatalogBindingDefinition> first = casting.findBinding(activityId, null);
        String provider = null;
        if (first.isPresent() && first.get().actorRole() != null) {
            provider = roleAssignments.get(first.get().actorRole());
        }
        return casting.findBinding(activityId, provider)
                .filter(b -> b.kind() == CatalogBindingKind.INVOKE
                        || b.kind() == CatalogBindingKind.SOFT
                        || b.kind() == CatalogBindingKind.WAIT
                        || b.kind() == CatalogBindingKind.CAPABILITY);
    }

    private ResolvedUi resolveBillerProductUi(Map<String, Object> context, CatalogBindingDefinition binding) {
        String billerCode = string(context, "billerCode");
        String productCode = string(context, "productCode", "billerProductCode");
        if (!StringUtils.hasText(billerCode) || !StringUtils.hasText(productCode)) {
            return ResolvedUi.formKey(
                    binding.formKey(),
                    binding.actorRole(),
                    CatalogFormSource.BILLER_PRODUCT,
                    FormKeyResolution.SHARED_BINDING
            );
        }
        try {
            BillerProduct product = billerService.requireProductByCodes(billerCode, productCode);
            String screen = binding.billerScreen() != null ? binding.billerScreen() : "LOOKUP";
            String screenKey = screen.trim().toUpperCase();
            List<ProductFieldDto> fields = switch (screenKey) {
                case "CONFIRM" -> uiSchemaService.resolveConfirmFields(product);
                case "AMOUNT" -> uiSchemaService.resolveAmountFields(product);
                case "LIST" -> uiSchemaService.resolveLookupFields(product);
                default -> uiSchemaService.resolveLookupFields(product);
            };
            return ResolvedUi.billerFields(
                    toFormFields(fields),
                    toFormio(fields),
                    binding.actorRole(),
                    binding.billerScreen()
            );
        } catch (Exception ex) {
            return ResolvedUi.formKey(
                    binding.formKey(),
                    binding.actorRole(),
                    CatalogFormSource.BILLER_PRODUCT,
                    FormKeyResolution.SHARED_BINDING
            );
        }
    }

    private static List<FormFieldDto> toFormFields(List<ProductFieldDto> fields) {
        List<FormFieldDto> out = new ArrayList<>();
        for (ProductFieldDto f : fields) {
            if (f == null || !StringUtils.hasText(f.name())) {
                continue;
            }
            out.add(FormFieldDto.builder()
                    .name(f.name())
                    .label(StringUtils.hasText(f.label()) ? f.label() : f.name())
                    .type(f.type() != null ? f.type() : "text")
                    .required(f.required())
                    .build());
        }
        return out;
    }

    private static Map<String, Object> toFormio(List<ProductFieldDto> fields) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("display", "form");
        List<Map<String, Object>> components = new ArrayList<>();
        for (ProductFieldDto f : fields) {
            if (f == null || !StringUtils.hasText(f.name())) {
                continue;
            }
            Map<String, Object> c = new LinkedHashMap<>();
            String type = f.type() != null ? f.type().toLowerCase() : "text";
            c.put("type", switch (type) {
                case "number" -> "number";
                case "tel", "phone" -> "phoneNumber";
                case "boolean", "checkbox" -> "checkbox";
                default -> "textfield";
            });
            c.put("key", f.name());
            c.put("label", StringUtils.hasText(f.label()) ? f.label() : f.name());
            c.put("input", true);
            if ("boolean".equals(type) || "checkbox".equals(type)) {
                c.put("defaultValue", false);
            }
            if (f.required()) {
                c.put("validate", Map.of("required", true));
            }
            if (StringUtils.hasText(f.placeholder())) {
                c.put("placeholder", f.placeholder());
            }
            components.add(c);
        }
        components.add(Map.of("type", "button", "key", "submit", "label", "Submit", "input", true, "hidden", true));
        schema.put("components", components);
        return schema;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> configurationFromContext(Map<String, Object> context) {
        if (context == null) {
            return Map.of();
        }
        Object raw = context.get("catalogConfiguration");
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    /** Version catalogue figée dans le context à l'init du paiement — porte les traductions. */
    private static UUID versionIdFromContext(Map<String, Object> context) {
        if (context == null) {
            return null;
        }
        Object raw = context.get("catalogServiceVersionId");
        if (raw == null || String.valueOf(raw).isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(raw));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> readRoleAssignments(Map<String, Object> context) {
        Map<String, String> out = new LinkedHashMap<>();
        if (context == null) {
            return out;
        }
        Object raw = context.get("roleAssignments");
        if (raw instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    out.put(String.valueOf(e.getKey()).toUpperCase(), String.valueOf(e.getValue()).toUpperCase());
                }
            }
        }
        return out;
    }

    private static String string(Map<String, Object> context, String... keys) {
        for (String key : keys) {
            Object v = context.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    public enum FormKeyResolution {
        /** binding.providerCode + formKey */
        EXPLICIT_PROVIDER,
        /** {product}-{role}-{provider} */
        CONVENTION,
        /** formKey unique pour tous les providers du rôle */
        SHARED_BINDING,
        BPMN
    }

    public record ResolvedUi(
            String formKey,
            List<FormFieldDto> fields,
            Map<String, Object> formSchema,
            String actorRole,
            CatalogFormSource formSource,
            String billerScreen,
            FormKeyResolution formKeyResolution
    ) {
        public static ResolvedUi formKey(
                String formKey,
                String actorRole,
                CatalogFormSource formSource,
                FormKeyResolution resolution
        ) {
            return new ResolvedUi(formKey, null, null, actorRole, formSource, null, resolution);
        }

        public static ResolvedUi billerFields(
                List<FormFieldDto> fields,
                Map<String, Object> formSchema,
                String actorRole,
                String billerScreen
        ) {
            return new ResolvedUi(
                    null, fields, formSchema, actorRole, CatalogFormSource.BILLER_PRODUCT, billerScreen, null);
        }

        public boolean hasInlineSchema() {
            return formSchema != null || (fields != null && !fields.isEmpty());
        }
    }
}
