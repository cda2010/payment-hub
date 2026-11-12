package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.domain.StepType;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.LocalizedMessages;
import com.gateway.payment.configuration.api.dto.CreateProviderFromTemplateRequest;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.api.dto.ProviderTemplateResponse;
import com.gateway.payment.configuration.domain.*;
import com.gateway.payment.configuration.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ProviderTemplateService {

    private final PaymentProviderRepository providerRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderAuthenticationRepository authenticationRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final PaymentFlowRepository flowRepository;
    private final PaymentFlowStepRepository stepRepository;
    private final ProviderStatusMappingRepository statusMappingRepository;
    private final ProviderOtpConfigRepository otpConfigRepository;
    private final ProviderWebhookConfigRepository webhookConfigRepository;
    private final ServiceTypeService serviceTypeService;
    private final AuditService auditService;

    public ProviderTemplateService(
            PaymentProviderRepository providerRepository,
            ProviderEnvironmentRepository environmentRepository,
            ProviderAuthenticationRepository authenticationRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            PaymentFlowRepository flowRepository,
            PaymentFlowStepRepository stepRepository,
            ProviderStatusMappingRepository statusMappingRepository,
            ProviderOtpConfigRepository otpConfigRepository,
            ProviderWebhookConfigRepository webhookConfigRepository,
            ServiceTypeService serviceTypeService,
            AuditService auditService
    ) {
        this.providerRepository = providerRepository;
        this.environmentRepository = environmentRepository;
        this.authenticationRepository = authenticationRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.flowRepository = flowRepository;
        this.stepRepository = stepRepository;
        this.statusMappingRepository = statusMappingRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.webhookConfigRepository = webhookConfigRepository;
        this.serviceTypeService = serviceTypeService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProviderTemplateResponse> listTemplates() {
        return List.of(
                new ProviderTemplateResponse(
                        "CORIS_OTP",
                        "Coris Money (OTP)",
                        "Mobile Money — OTP génération + confirmation",
                        PaymentMethodType.MOBILE_MONEY,
                        ExecutionMode.OTP,
                        ""
                ),
                new ProviderTemplateResponse(
                        "ORANGE_OTP",
                        "Orange Money (OTP)",
                        "Mobile Money — saisie directe du code OTP",
                        PaymentMethodType.MOBILE_MONEY,
                        ExecutionMode.OTP,
                        ""
                ),
                new ProviderTemplateResponse(
                        "WAVE_REDIRECT",
                        "Wave (Redirect)",
                        "Mobile Money — redirect checkout puis callback",
                        PaymentMethodType.MOBILE_MONEY,
                        ExecutionMode.REDIRECT,
                        ""
                ),
                new ProviderTemplateResponse(
                        "CARD_REDIRECT",
                        "Paiement par carte (3DS)",
                        "Carte bancaire — redirection page 3DS / hosted checkout puis callback",
                        PaymentMethodType.CARD,
                        ExecutionMode.REDIRECT,
                        ""
                ),
                new ProviderTemplateResponse(
                        "BANK_TRANSFER_ASYNC",
                        "Compte bancaire (virement)",
                        "Virement — saisie IBAN puis attente confirmation (callback)",
                        PaymentMethodType.BANK_TRANSFER,
                        ExecutionMode.ASYNC,
                        ""
                ),
                new ProviderTemplateResponse(
                        "WALLET_TRANSFER",
                        "Transfert multi-rails",
                        "Wave→Coris, Coris→Coris, Banque→Coris… — OTP puis exécution",
                        PaymentMethodType.OTHER,
                        ExecutionMode.OTP,
                        ""
                ),
                new ProviderTemplateResponse(
                        "BILL_PAY",
                        "Paiement de factures",
                        "Lookup facture + OTP + paiement — connecteur bill-payment-connector (:9182)",
                        PaymentMethodType.OTHER,
                        ExecutionMode.OTP,
                        "http://localhost:9182"
                )
        );
    }

    @Transactional
    public ProviderResponse createFromTemplate(CreateProviderFromTemplateRequest request, String actor) {
        if (providerRepository.existsByCode(request.code())) {
            throw new BusinessException("PROVIDER_CODE_EXISTS", "Code fournisseur déjà utilisé");
        }

        String template = request.templateCode().trim().toUpperCase(Locale.ROOT);
        PaymentProvider provider = switch (template) {
            case "CORIS_OTP" -> seedCoris(request);
            case "ORANGE_OTP" -> seedOrange(request);
            case "WAVE_REDIRECT" -> seedWave(request);
            case "CARD_REDIRECT" -> seedCard(request);
            case "BANK_TRANSFER_ASYNC" -> seedBank(request);
            case "WALLET_TRANSFER" -> seedWalletTransfer(request);
            case "BILL_PAY" -> seedBillPay(request);
            default -> throw new BusinessException("TEMPLATE_NOT_FOUND",
                    "Modèle inconnu: " + request.templateCode(), HttpStatus.BAD_REQUEST);
        };

        auditService.record(actor, "PROVIDER_CREATE_FROM_TEMPLATE", "PROVIDER",
                provider.getId().toString(), "Template " + template + " → " + provider.getCode());

        return toProviderResponse(provider);
    }

    @Transactional
    public void seedDemoProviders() {
        // Plus de providers mock:// — seuls les connecteurs réels (ex. bill-payment :9182).
        if (!providerRepository.existsByCode("BILL_PAYMENT")) {
            seedBillPay(demoRequest("BILL_PAY", "BILL_PAYMENT", "Paiement de factures", "http://localhost:9182", 7));
        }
        enrichFseIdentities();
    }

    /** Identité FSE optionnelle — n'écrase que si code_fse encore vide. */
    private void enrichFseIdentities() {
        applyFseIfMissing("CORIS_MONEY", "0011", "0011", "CORIS", "BF",
                FseParticipantKind.WALLET, FseStatut.NONBANQUE);
        applyFseIfMissing("CORIS_BANK", "0004", "0004", "CORIS", "BF",
                FseParticipantKind.BANQUE, FseStatut.BANQUE);
    }

    private void applyFseIfMissing(
            String providerCode,
            String codeFse,
            String codeExterne,
            String groupCode,
            String countryCode,
            FseParticipantKind kind,
            FseStatut statut
    ) {
        providerRepository.findByCode(providerCode).ifPresent(p -> {
            if (p.getCodeFse() != null && !p.getCodeFse().isBlank()) {
                return;
            }
            if (providerRepository.existsByCodeFse(codeFse)
                    && providerRepository.findByCodeFse(codeFse).map(o -> !o.getId().equals(p.getId())).orElse(false)) {
                return;
            }
            p.setCodeFse(codeFse);
            p.setCodeExterne(codeExterne);
            p.setGroupCode(groupCode);
            p.setCountryCode(countryCode);
            p.setParticipantKind(kind);
            p.setFseStatut(statut);
            providerRepository.save(p);
        });
    }

    private CreateProviderFromTemplateRequest demoRequest(
            String template, String code, String libelle, String baseUrl, int ordre
    ) {
        return new CreateProviderFromTemplateRequest(
                template, code, libelle, "Fournisseur de démonstration",
                "SANDBOX", baseUrl, true, ordre
        );
    }

    private ProviderResponse toProviderResponse(PaymentProvider provider) {
        List<ServiceTypeCode> types = provider.getServiceTypes() == null
                ? List.of()
                : provider.getServiceTypes().stream()
                .map(ServiceType::getCode)
                .sorted(Comparator.comparing(Enum::name))
                .toList();
        return new ProviderResponse(
                provider.getId(),
                provider.getCode(),
                provider.getLibelle(),
                provider.getDescription(),
                provider.getLogoUrl(),
                provider.isActif(),
                provider.getOrdreAffichage(),
                provider.getPaymentMethodType() != null
                        ? provider.getPaymentMethodType()
                        : PaymentMethodType.MOBILE_MONEY,
                provider.getModeExecution(),
                types,
                provider.getCodeFse(),
                provider.getCodeExterne(),
                provider.getGroupCode(),
                provider.getCountryCode(),
                provider.getParticipantKind(),
                provider.getFseStatut(),
                provider.getCreatedAt(),
                provider.getUpdatedAt()
        );
    }

    private void attachServiceTypes(PaymentProvider provider, ServiceTypeCode... codes) {
        provider.getServiceTypes().clear();
        provider.getServiceTypes().addAll(serviceTypeService.resolveEntities(Arrays.asList(codes)));
        providerRepository.save(provider);
    }

    private PaymentFlow saveFlow(
            PaymentProvider provider,
            ServiceTypeCode serviceType,
            String nom,
            String bpmProcessKey
    ) {
        return flowRepository.save(PaymentFlow.builder()
                .provider(provider)
                .code(serviceType.name())
                .nom(nom)
                .serviceType(serviceType)
                .bpmProcessKey(bpmProcessKey)
                .actif(true)
                .build());
    }

    /**
     * Un flow par type de service. {@code primaryBpm} s'applique au premier type ;
     * les suivants utilisent {@link ServiceTypeCode#defaultBpmProcessKey()}.
     */
    private void seedFlowsForServiceTypes(
            PaymentProvider provider,
            String primaryBpm,
            ServiceTypeCode primary,
            ServiceTypeCode... extras
    ) {
        saveFlow(provider, primary, flowLabel(primary), primaryBpm);
        for (ServiceTypeCode st : extras) {
            saveFlow(provider, st, flowLabel(st), st.defaultBpmProcessKey());
        }
    }

    private static String flowLabel(ServiceTypeCode st) {
        return switch (st) {
            case PAYEMENT -> "Paiement / checkout";
            case TRANSFERT -> "Transfert";
            case PAIEMENT_FACTURE -> "Paiement facture";
            case CONSULTATION_SOLDE -> "Consultation solde";
            case CONSULTATIONHIST -> "Consultation historique";
            case INFOCLIENT -> "Info client";
            case LINK -> "Lien compte";
            case UNLINK -> "Déliaison compte";
            case DEFAUT -> "Par défaut";
        };
    }

    private PaymentProvider seedCoris(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.MOBILE_MONEY)
                .modeExecution(ExecutionMode.OTP)
                .participantKind(FseParticipantKind.WALLET)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());

        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.API_KEY).customHeaderName("X-API-Key").build());

        ProviderOperation gen = op(p, "GENERATE_OTP", "Générer OTP", true);
        ProviderOperation pay = op(p, "CONFIRM_PAYMENT", "Confirmer paiement", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);

        api(p, env, gen, "GENERATE_OTP", "POST", "api/gateway/otp",
                "{\"telephone\":\"${phone}\",\"msisdn\":\"${phone}\",\"codeFse\":\"${codeFse}\",\"amount\":\"${amount}\",\"currency\":\"${currency}\"}",
                "application/x-www-form-urlencoded");
        api(p, env, pay, "CONFIRM_PAYMENT", "POST", "/payments",
                "{\"msisdn\":\"${phone}\",\"otp\":\"${otp}\",\"transactionAmount\":\"${amount}\",\"currencyCode\":\"${currency}\"}");
        api(p, env, status, "GET_STATUS", "GET", "/payments/${providerReference}", null);

        // Orchestration = BPMN (userTasks formKey → forms/*.json). Pas de payment_flow_step.
        seedFlowsForServiceTypes(p, "providerOtpCheckout",
                ServiceTypeCode.PAYEMENT, ServiceTypeCode.CONSULTATION_SOLDE, ServiceTypeCode.LINK,
                ServiceTypeCode.TRANSFERT);

        mapDefaultStatuses(p);
        otpConfigRepository.save(ProviderOtpConfig.builder()
                .provider(p).otpRequired(true).length(6).validitySeconds(120).maxAttempts(3)
                .resendAllowed(true).resendDelaySeconds(30)
                .generateOperation("GENERATE_OTP").validateOperation("CONFIRM_PAYMENT").build());
        webhook(p, slug(request.code(), "coris-money"));
        attachServiceTypes(p, ServiceTypeCode.PAYEMENT, ServiceTypeCode.CONSULTATION_SOLDE,
                ServiceTypeCode.LINK, ServiceTypeCode.TRANSFERT);
        return p;
    }

    private PaymentProvider seedOrange(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.MOBILE_MONEY)
                .modeExecution(ExecutionMode.OTP)
                .participantKind(FseParticipantKind.WALLET)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());

        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.OAUTH2_CLIENT_CREDENTIALS)
                .tokenUrl(request.baseUrl().startsWith("mock://")
                        ? request.baseUrl() + "/token"
                        : request.baseUrl() + "/oauth/token")
                .build());

        ProviderOperation pay = op(p, "CONFIRM_PAYMENT", "Payer", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        api(p, env, pay, "CONFIRM_PAYMENT", "POST", "/payments",
                "{\"otp\":\"${otp}\",\"amount\":\"${amount}\",\"currency\":\"${currency}\",\"order_id\":\"${paymentId}\"}");
        api(p, env, status, "GET_STATUS", "GET", "/payments/${providerReference}", null);

        seedFlowsForServiceTypes(p, "providerOrangeOtp",
                ServiceTypeCode.PAYEMENT, ServiceTypeCode.CONSULTATION_SOLDE);

        mapDefaultStatuses(p);
        otpConfigRepository.save(ProviderOtpConfig.builder()
                .provider(p).otpRequired(true).length(6).validitySeconds(180).maxAttempts(3)
                .resendAllowed(false).resendDelaySeconds(0)
                .validateOperation("CONFIRM_PAYMENT").build());
        webhook(p, slug(request.code(), "orange-money"));
        attachServiceTypes(p, ServiceTypeCode.PAYEMENT, ServiceTypeCode.CONSULTATION_SOLDE);
        return p;
    }

    private PaymentProvider seedWave(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.MOBILE_MONEY)
                .modeExecution(ExecutionMode.REDIRECT)
                .participantKind(FseParticipantKind.WALLET)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());

        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.BEARER_TOKEN).build());

        ProviderOperation create = op(p, "CREATE_PAYMENT", "Créer paiement", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        api(p, env, create, "CREATE_PAYMENT", "POST", "/v1/checkout/sessions",
                "{\"amount\":\"${amount}\",\"currency\":\"${currency}\",\"client_reference\":\"${paymentId}\"}");
        api(p, env, status, "GET_STATUS", "GET", "/v1/checkout/sessions/${providerReference}", null);

        seedFlowsForServiceTypes(p, "providerRedirectCheckout", ServiceTypeCode.PAYEMENT);

        mapDefaultStatuses(p);
                statusMappingRepository.save(ProviderStatusMapping.builder()
                        .provider(p).providerStatus("succeeded").gatewayStatus(PaymentStatus.SUCCESS)
                        .messages(LocalizedMessages.of("Succès", "Success")).build());
        webhook(p, slug(request.code(), "wave"));
        attachServiceTypes(p, ServiceTypeCode.PAYEMENT);
        return p;
    }

    private PaymentProvider seedCard(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.CARD)
                .modeExecution(ExecutionMode.REDIRECT)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());
        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.API_KEY).customHeaderName("X-API-Key").build());

        ProviderOperation create = op(p, "CREATE_PAYMENT", "Créer session carte", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        api(p, env, create, "CREATE_PAYMENT", "POST", "/v1/card/sessions",
                "{\"amount\":\"${amount}\",\"currency\":\"${currency}\",\"order_id\":\"${paymentId}\"}");
        api(p, env, status, "GET_STATUS", "GET", "/v1/card/sessions/${providerReference}", null);

        seedFlowsForServiceTypes(p, "providerRedirectCheckout", ServiceTypeCode.PAYEMENT);

        mapDefaultStatuses(p);
                statusMappingRepository.save(ProviderStatusMapping.builder()
                        .provider(p).providerStatus("succeeded").gatewayStatus(PaymentStatus.SUCCESS)
                        .messages(LocalizedMessages.of("Succès", "Success")).build());
        webhook(p, slug(request.code(), "card-payment"));
        attachServiceTypes(p, ServiceTypeCode.PAYEMENT);
        return p;
    }

    private PaymentProvider seedBank(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.BANK_TRANSFER)
                .modeExecution(ExecutionMode.ASYNC)
                .participantKind(FseParticipantKind.BANQUE)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());
        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.NONE).build());

        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        api(p, env, status, "GET_STATUS", "GET", "/v1/transfers/${providerReference}", null);

        seedFlowsForServiceTypes(p, "providerBankTransfer",
                ServiceTypeCode.TRANSFERT, ServiceTypeCode.CONSULTATION_SOLDE);

        mapDefaultStatuses(p);
                statusMappingRepository.save(ProviderStatusMapping.builder()
                        .provider(p).providerStatus("succeeded").gatewayStatus(PaymentStatus.SUCCESS)
                        .messages(LocalizedMessages.of("Succès", "Success")).build());
        webhook(p, slug(request.code(), "bank-transfer"));
        attachServiceTypes(p, ServiceTypeCode.TRANSFERT, ServiceTypeCode.CONSULTATION_SOLDE);
        return p;
    }

    private PaymentProvider seedWalletTransfer(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.OTHER)
                .modeExecution(ExecutionMode.OTP)
                .participantKind(FseParticipantKind.RESEAU)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());
        authenticationRepository.save(ProviderAuthentication.builder()
                .provider(p).environment(env).authType(AuthType.NONE).build());

        ProviderOperation gen = op(p, "GENERATE_OTP", "OTP confirmation transfert", true);
        ProviderOperation exec = op(p, "TRANSFER_EXECUTE", "Exécuter transfert", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        api(p, env, gen, "GENERATE_OTP", "POST", "/transfer/otp",
                "{\"source\":\"${sourceRail}\",\"destination\":\"${destRail}\",\"msisdn\":\"${destPhone}\"}");
        api(p, env, exec, "TRANSFER_EXECUTE", "POST", "/transfer/execute",
                "{\"source\":\"${sourceRail}\",\"destination\":\"${destRail}\",\"destPhone\":\"${destPhone}\","
                        + "\"amount\":\"${amount}\",\"currency\":\"${currency}\",\"otp\":\"${otp}\"}");
        api(p, env, status, "GET_STATUS", "GET", "/transfer/${providerReference}", null);

        seedFlowsForServiceTypes(p, "providerWalletTransfer", ServiceTypeCode.TRANSFERT);

        mapDefaultStatuses(p);
        otpConfigRepository.save(ProviderOtpConfig.builder()
                .provider(p).otpRequired(true).length(6).validitySeconds(120).maxAttempts(3)
                .resendAllowed(true).resendDelaySeconds(30)
                .generateOperation("GENERATE_OTP").validateOperation("TRANSFER_EXECUTE").build());
        webhook(p, slug(request.code(), "wallet-transfer"));
        attachServiceTypes(p, ServiceTypeCode.TRANSFERT);
        return p;
    }

    private PaymentProvider seedBillPay(CreateProviderFromTemplateRequest request) {
        PaymentProvider p = providerRepository.save(PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(PaymentMethodType.OTHER)
                .modeExecution(ExecutionMode.OTP)
                .participantKind(FseParticipantKind.FACTURIER)
                .build());

        ProviderEnvironment env = environment(p, request.environmentCode(), request.baseUrl());
        // Auth API_KEY — bill-payment-connector (X-API-Key / Bearer)
        for (ProviderEnvironment e : environmentRepository.findByProviderId(p.getId())) {
            authenticationRepository.save(ProviderAuthentication.builder()
                    .provider(p).environment(e)
                    .authType(AuthType.API_KEY)
                    .customHeaderName("X-API-Key")
                    .build());
        }

        ProviderOperation lookup = op(p, "LOOKUP_BILL", "Consulter facture", true);
        ProviderOperation list = op(p, "BILL_LIST", "Lister factures", true);
        ProviderOperation gen = op(p, "GENERATE_OTP", "OTP paiement facture", true);
        ProviderOperation pay = op(p, "PAY_BILL", "Payer facture", true);
        ProviderOperation status = op(p, "GET_STATUS", "Statut", true);
        String billBody = "{\"biller\":\"${billerCode}\",\"billerCode\":\"${billerCode}\","
                + "\"reference\":\"${billReference}\",\"billReference\":\"${billReference}\","
                + "\"productCode\":\"${productCode}\"}";
        api(p, env, lookup, "LOOKUP_BILL", "POST", "/bills/lookup", billBody);
        api(p, env, list, "BILL_LIST", "POST", "/bills/list",
                "{\"biller\":\"${billerCode}\",\"billerCode\":\"${billerCode}\","
                        + "\"productCode\":\"${productCode}\","
                        + "\"subscriberReference\":\"${subscriberReference}\"}");
        api(p, env, gen, "GENERATE_OTP", "POST", "/bills/otp", billBody);
        api(p, env, pay, "PAY_BILL", "POST", "/bills/pay",
                "{\"biller\":\"${billerCode}\",\"billerCode\":\"${billerCode}\","
                        + "\"reference\":\"${billReference}\",\"billReference\":\"${billReference}\","
                        + "\"productCode\":\"${productCode}\","
                        + "\"amount\":\"${amount}\",\"otp\":\"${otp}\",\"currency\":\"${currency}\","
                        + "\"idempotencyKey\":\"${paymentId}\","
                        + "\"transactionReference\":\"${paymentId}\"}");
        api(p, env, status, "GET_STATUS", "GET",
                "/bills/${providerReference}?biller=${billerCode}&productCode=${productCode}", null);

        seedFlowsForServiceTypes(p, "providerBillPay",
                ServiceTypeCode.PAIEMENT_FACTURE, ServiceTypeCode.PAYEMENT);
        // PAYEMENT sur ce rail partage le BPMN facture (déjà créé avec defaultBpm = otp)
        flowRepository.findByProviderIdAndServiceType(p.getId(), ServiceTypeCode.PAYEMENT)
                .ifPresent(f -> {
                    f.setBpmProcessKey("providerBillPay");
                    f.setNom("Paiement (rail facture)");
                    flowRepository.save(f);
                });

        mapDefaultStatuses(p);
        otpConfigRepository.save(ProviderOtpConfig.builder()
                .provider(p).otpRequired(true).length(6).validitySeconds(120).maxAttempts(3)
                .resendAllowed(true).resendDelaySeconds(30)
                .generateOperation("GENERATE_OTP").validateOperation("PAY_BILL").build());
        webhook(p, slug(request.code(), "bill-payment"));
        attachServiceTypes(p, ServiceTypeCode.PAIEMENT_FACTURE, ServiceTypeCode.PAYEMENT);
        return p;
    }

    private ProviderEnvironment environment(PaymentProvider provider, String code, String baseUrl) {
        String primaryCode = ProviderEnvironmentCode.parse(code).name();
        ProviderEnvironment primary = environmentRepository.save(ProviderEnvironment.builder()
                .provider(provider)
                .code(primaryCode)
                .baseUrl(baseUrl)
                .timeoutSeconds(30)
                .actif(true)
                .build());
        for (ProviderEnvironmentCode std : ProviderEnvironmentCode.values()) {
            if (std.name().equals(primaryCode)) {
                continue;
            }
            if (environmentRepository.findByProviderIdAndCode(provider.getId(), std.name()).isEmpty()) {
                environmentRepository.save(ProviderEnvironment.builder()
                        .provider(provider)
                        .code(std.name())
                        .baseUrl(baseUrl)
                        .timeoutSeconds(30)
                        .actif(false)
                        .build());
            }
        }
        return primary;
    }

    private ProviderOperation op(PaymentProvider p, String code, String nom, boolean idempotent) {
        return operationRepository.save(ProviderOperation.builder()
                .provider(p).code(code).nom(nom).idempotent(idempotent).actif(true).build());
    }

    private void api(PaymentProvider p, ProviderEnvironment env, ProviderOperation op,
                     String code, String method, String path, String mapping) {
        api(p, env, op, code, method, path, mapping, "application/json");
    }

    private void api(PaymentProvider p, ProviderEnvironment env, ProviderOperation op,
                     String code, String method, String path, String mapping, String contentType) {
        apiRepository.save(ProviderApi.builder()
                .provider(p).environment(env).operation(op).code(code).nom(code)
                .httpMethod(method).path(path).contentType(contentType)
                .timeoutSeconds(30).requestMappingJson(mapping)
                .retryEnabled(false).maxAttempts(1).retryDelayMs(2000L).backoffMultiplier(1.0)
                .actif(true).build());
    }

    private void step(PaymentFlow flow, String code, StepType type, int ordre, String apiOp,
                      String nextOk, String nextKo, String config) {
        stepRepository.save(PaymentFlowStep.builder()
                .flow(flow).code(code).stepType(type).ordre(ordre)
                .apiOperationCode(apiOp).nextStepSuccess(nextOk).nextStepFailure(nextKo)
                .configurationJson(config).actif(true).build());
    }

    /** configurationJson Phase 2 — schéma Form.io natif (+ bouton submit masqué). */
    private static String formioPhone() {
        return """
                {"formio":{"display":"form","components":[\
                {"type":"phoneNumber","key":"phoneNumber","label":"Numéro de téléphone","input":true,"validate":{"required":true}},\
                {"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}\
                ]}}""";
    }

    private static String formioBankDetails() {
        return """
                {"formio":{"display":"form","components":[\
                {"type":"textfield","key":"accountHolder","label":"Titulaire du compte","input":true,"validate":{"required":true}},\
                {"type":"textfield","key":"iban","label":"IBAN / N° de compte","input":true,"validate":{"required":true}},\
                {"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}\
                ]}}""";
    }

    private static String formioWalletTransfer() {
        return """
                {"formio":{"display":"form","components":[\
                {"type":"select","key":"sourceRail","label":"Compte source","input":true,"dataSrc":"values",\
                "data":{"values":[{"label":"WAVE","value":"WAVE"},{"label":"CORIS_MONEY","value":"CORIS_MONEY"},{"label":"BANK_TRANSFER","value":"BANK_TRANSFER"}]},\
                "widget":"choicesjs","validate":{"required":true}},\
                {"type":"select","key":"destRail","label":"Compte destination","input":true,"dataSrc":"values",\
                "data":{"values":[{"label":"CORIS_MONEY","value":"CORIS_MONEY"},{"label":"WAVE","value":"WAVE"},{"label":"BANK_TRANSFER","value":"BANK_TRANSFER"}]},\
                "widget":"choicesjs","validate":{"required":true}},\
                {"type":"phoneNumber","key":"destPhone","label":"Téléphone / compte destinataire","input":true,"validate":{"required":true}},\
                {"type":"number","key":"amount","label":"Montant","input":true,"validate":{"required":true}},\
                {"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}\
                ]}}""";
    }

    private static String formioBillCollect() {
        return """
                {"formio":{"display":"form","components":[\
                {"type":"select","key":"billerCode","label":"Facturier","input":true,"dataSrc":"values",\
                "data":{"values":[{"label":"SONABEL","value":"SONABEL"},{"label":"ONEA","value":"ONEA"},{"label":"CANAL+","value":"CANAL+"},{"label":"IMPOTS","value":"IMPOTS"}]},\
                "widget":"choicesjs","validate":{"required":true}},\
                {"type":"textfield","key":"billReference","label":"N° contrat / facture","input":true,"validate":{"required":true}},\
                {"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}\
                ]}}""";
    }

    private static String formioBillConfirm() {
        return """
                {"formio":{"display":"form","components":[\
                {"type":"checkbox","key":"confirmPay","label":"Je confirme le montant et le paiement de cette facture","input":true,"defaultValue":false,"validate":{"required":true}},\
                {"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}\
                ]}}""";
    }

    private void mapDefaultStatuses(PaymentProvider p) {
        List.of(
                mapping(p, "00", PaymentStatus.SUCCESS, "Succès", "Success"),
                mapping(p, "01", PaymentStatus.FAILED, "Erreur", "Error"),
                mapping(p, "02", PaymentStatus.PENDING, "En attente", "Pending"),
                mapping(p, "SUCCESS", PaymentStatus.SUCCESS, "Succès", "Success"),
                mapping(p, "FAILED", PaymentStatus.FAILED, "Échec", "Failed"),
                mapping(p, "PENDING", PaymentStatus.PENDING, "En attente", "Pending")
        ).forEach(statusMappingRepository::save);
    }

    private ProviderStatusMapping mapping(
            PaymentProvider p,
            String code,
            PaymentStatus status,
            String messageFr,
            String messageEn
    ) {
        return ProviderStatusMapping.builder()
                .provider(p)
                .providerStatus(code)
                .gatewayStatus(status)
                .messages(LocalizedMessages.of(messageFr, messageEn))
                .build();
    }

    private void webhook(PaymentProvider p, String pathSuffix) {
        webhookConfigRepository.save(ProviderWebhookConfig.builder()
                .provider(p).pathSuffix(pathSuffix).authMode("NONE").actif(true).build());
    }

    private String slug(String code, String fallback) {
        return code.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
