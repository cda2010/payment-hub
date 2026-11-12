package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.LocalizedMessages;
import com.gateway.payment.configuration.api.dto.ProviderConfigExportResponse;
import com.gateway.payment.configuration.api.dto.ProviderConfigExportResponse.*;
import com.gateway.payment.configuration.api.dto.ProviderConfigImportRequest;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.domain.*;
import com.gateway.payment.configuration.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ProviderConfigExportService {

    private static final int EXPORT_VERSION = 2;

    private final PaymentProviderRepository providerRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderAuthenticationRepository authenticationRepository;
    private final ProviderFseApiRepository fseApiRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final PaymentFlowRepository flowRepository;
    private final PaymentFlowStepRepository stepRepository;
    private final ProviderStatusMappingRepository statusMappingRepository;
    private final ProviderOtpConfigRepository otpConfigRepository;
    private final ProviderWebhookConfigRepository webhookConfigRepository;
    private final ProviderService providerService;
    private final ServiceTypeService serviceTypeService;
    private final AuditService auditService;

    public ProviderConfigExportService(
            PaymentProviderRepository providerRepository,
            ProviderEnvironmentRepository environmentRepository,
            ProviderAuthenticationRepository authenticationRepository,
            ProviderFseApiRepository fseApiRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            PaymentFlowRepository flowRepository,
            PaymentFlowStepRepository stepRepository,
            ProviderStatusMappingRepository statusMappingRepository,
            ProviderOtpConfigRepository otpConfigRepository,
            ProviderWebhookConfigRepository webhookConfigRepository,
            ProviderService providerService,
            ServiceTypeService serviceTypeService,
            AuditService auditService
    ) {
        this.providerRepository = providerRepository;
        this.environmentRepository = environmentRepository;
        this.authenticationRepository = authenticationRepository;
        this.fseApiRepository = fseApiRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.flowRepository = flowRepository;
        this.stepRepository = stepRepository;
        this.statusMappingRepository = statusMappingRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.webhookConfigRepository = webhookConfigRepository;
        this.providerService = providerService;
        this.serviceTypeService = serviceTypeService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ProviderConfigExportResponse export(UUID providerId) {
        PaymentProvider provider = providerRepository.findByIdWithServiceTypes(providerId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable",
                        HttpStatus.NOT_FOUND));
        List<ProviderEnvironment> environments = environmentRepository.findByProviderId(providerId);
        Map<UUID, String> envCodes = new HashMap<>();
        environments.forEach(env -> envCodes.put(env.getId(), env.getCode()));

        List<ProviderOperation> operations = operationRepository.findByProviderId(providerId);
        Map<UUID, String> opCodes = new HashMap<>();
        operations.forEach(op -> opCodes.put(op.getId(), op.getCode()));

        List<AuthenticationExport> auths = environments.stream()
                .map(env -> authenticationRepository.findByProviderIdAndEnvironmentId(providerId, env.getId())
                        .map(auth -> new AuthenticationExport(
                                env.getCode(),
                                auth.getAuthType(),
                                auth.getClientId(),
                                auth.getTokenUrl(),
                                auth.getCustomHeaderName(),
                                auth.getExtraConfigJson()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<ProviderFseApi> fseApis = fseApiRepository.findByProviderId(providerId);
        Map<UUID, String> fseApiCodes = new HashMap<>();
        fseApis.forEach(fa -> fseApiCodes.put(fa.getId(), fa.getCode()));

        List<FseApiExport> fseApiExports = fseApis.stream()
                .map(fa -> new FseApiExport(
                        fa.getCode(),
                        fa.getNom(),
                        fa.getTypeApi(),
                        fa.getBaseUrl(),
                        fa.getHost(),
                        fa.getPort(),
                        fa.isUseSsl(),
                        fa.isUseProxy(),
                        fa.getEnvironment() != null ? fa.getEnvironment().getCode() : null,
                        fa.isActif(),
                        fa.getAuthType(),
                        fa.getClientId(),
                        fa.getTokenUrl(),
                        fa.getCustomHeaderName(),
                        fa.getExtraConfigJson()
                ))
                .toList();

        List<FlowExport> flows = flowRepository.findByProviderId(providerId).stream()
                .map(flow -> new FlowExport(
                        flow.getCode(),
                        flow.getNom(),
                        flow.getServiceType(),
                        flow.getBpmProcessKey(),
                        flow.getDescription(),
                        flow.isActif(),
                        stepRepository.findByFlowIdOrderByOrdreAsc(flow.getId()).stream()
                                .map(step -> new FlowStepExport(
                                        step.getCode(),
                                        step.getStepType(),
                                        step.getOrdre(),
                                        step.getApiOperationCode(),
                                        step.getNextStepSuccess(),
                                        step.getNextStepFailure(),
                                        step.getConfigurationJson(),
                                        step.isActif()
                                ))
                                .toList()
                ))
                .toList();

        OtpConfigExport otp = otpConfigRepository.findByProviderId(providerId)
                .map(cfg -> new OtpConfigExport(
                        cfg.isOtpRequired(),
                        cfg.getLength(),
                        cfg.getValiditySeconds(),
                        cfg.getMaxAttempts(),
                        cfg.isResendAllowed(),
                        cfg.getResendDelaySeconds(),
                        cfg.getGenerateOperation(),
                        cfg.getValidateOperation()
                ))
                .orElse(null);

        return new ProviderConfigExportResponse(
                EXPORT_VERSION,
                Instant.now().toString(),
                new ProviderExport(
                        provider.getCode(),
                        provider.getLibelle(),
                        provider.getDescription(),
                        provider.getLogoUrl(),
                        provider.isActif(),
                        provider.getOrdreAffichage(),
                        provider.getPaymentMethodType() != null
                                ? provider.getPaymentMethodType()
                                : com.gateway.payment.common.domain.PaymentMethodType.MOBILE_MONEY,
                        provider.getModeExecution(),
                        provider.getServiceTypes() == null ? List.of()
                                : provider.getServiceTypes().stream()
                                .map(ServiceType::getCode)
                                .sorted()
                                .toList(),
                        provider.getCodeFse(),
                        provider.getCodeExterne(),
                        provider.getGroupCode(),
                        provider.getCountryCode(),
                        provider.getParticipantKind(),
                        provider.getFseStatut()
                ),
                environments.stream()
                        .map(env -> new EnvironmentExport(
                                env.getCode(),
                                env.getBaseUrl(),
                                env.getTimeoutSeconds(),
                                env.isActif()
                        ))
                        .toList(),
                auths,
                fseApiExports,
                operations.stream()
                        .map(op -> new OperationExport(
                                op.getCode(),
                                op.getNom(),
                                op.getDescription(),
                                op.isIdempotent(),
                                op.isActif()
                        ))
                        .toList(),
                apiRepository.findByProviderId(providerId).stream()
                        .map(api -> new ApiExport(
                                api.getCode(),
                                api.getNom(),
                                api.getEnvironment() != null ? envCodes.get(api.getEnvironment().getId()) : null,
                                api.getFseApi() != null ? fseApiCodes.get(api.getFseApi().getId()) : null,
                                opCodes.get(api.getOperation().getId()),
                                api.getHttpMethod(),
                                api.getPath(),
                                api.getContentType(),
                                api.getTimeoutSeconds(),
                                api.getRequestMappingJson(),
                                api.getResponseMappingJson(),
                                api.isRetryEnabled(),
                                api.getMaxAttempts(),
                                api.getRetryDelayMs(),
                                api.getBackoffMultiplier(),
                                api.getRetryOn(),
                                api.isActif()
                        ))
                        .toList(),
                flows,
                statusMappingRepository.findByProviderId(providerId).stream()
                        .map(m -> new StatusMappingExport(
                                m.getProviderStatus(),
                                m.getGatewayStatus(),
                                m.getMessages()
                        ))
                        .toList(),
                otp,
                webhookConfigRepository.findByProviderId(providerId).stream()
                        .map(w -> new WebhookExport(
                                w.getPathSuffix(),
                                w.getAuthMode(),
                                w.getAllowedIps(),
                                w.isActif()
                        ))
                        .toList()
        );
    }

    @Transactional
    public ProviderResponse importConfig(ProviderConfigImportRequest request, String actor) {
        ProviderConfigExportResponse config = request.config();
        if (config.provider() == null) {
            throw new BusinessException("IMPORT_INVALID", "Configuration provider manquante", HttpStatus.BAD_REQUEST);
        }

        Optional<PaymentProvider> existing = providerRepository.findByCode(config.provider().code());
        if (existing.isPresent() && !request.overwrite()) {
            throw new BusinessException("PROVIDER_CODE_EXISTS",
                    "Le fournisseur " + config.provider().code() + " existe déjà. Utilisez overwrite=true.",
                    HttpStatus.CONFLICT);
        }

        PaymentProvider provider = existing.orElseGet(() -> PaymentProvider.builder().build());
        applyProvider(provider, config.provider());
        provider = providerRepository.save(provider);

        if (existing.isPresent()) {
            clearProviderConfig(provider.getId());
        }

        Map<String, ProviderEnvironment> envByCode = new HashMap<>();
        for (EnvironmentExport envExport : config.environments()) {
            String code = ProviderEnvironmentCode.parse(envExport.code()).name();
            ProviderEnvironment env = environmentRepository.save(ProviderEnvironment.builder()
                    .provider(provider)
                    .code(code)
                    .baseUrl(envExport.baseUrl())
                    .timeoutSeconds(envExport.timeoutSeconds())
                    .actif(envExport.actif())
                    .build());
            envByCode.put(code, env);
            if (envExport.code() != null) {
                envByCode.put(envExport.code(), env);
            }
        }

        // Compléter les environnements standard manquants (inactifs)
        String defaultUrl = envByCode.values().stream()
                .map(ProviderEnvironment::getBaseUrl)
                .filter(u -> u != null && !u.isBlank())
                .findFirst()
                .orElse("https://example.invalid");
        boolean hasActive = envByCode.values().stream().anyMatch(ProviderEnvironment::isActif);
        for (ProviderEnvironmentCode std : ProviderEnvironmentCode.values()) {
            if (environmentRepository.findByProviderIdAndCode(provider.getId(), std.name()).isPresent()) {
                continue;
            }
            ProviderEnvironment created = environmentRepository.save(ProviderEnvironment.builder()
                    .provider(provider)
                    .code(std.name())
                    .baseUrl(defaultUrl)
                    .timeoutSeconds(30)
                    .actif(!hasActive && std == ProviderEnvironmentCode.SANDBOX)
                    .build());
            envByCode.put(created.getCode(), created);
            if (created.isActif()) {
                hasActive = true;
            }
        }

        for (AuthenticationExport authExport : config.authentications()) {
            ProviderEnvironment env = envByCode.get(authExport.environmentCode());
            if (env == null) {
                continue;
            }
            authenticationRepository.save(ProviderAuthentication.builder()
                    .provider(provider)
                    .environment(env)
                    .authType(authExport.authType())
                    .clientId(authExport.clientId())
                    .tokenUrl(authExport.tokenUrl())
                    .customHeaderName(authExport.customHeaderName())
                    .extraConfigJson(authExport.extraConfigJson())
                    .build());
        }

        Map<String, ProviderFseApi> fseApiByCode = new HashMap<>();
        List<FseApiExport> fseApiExports = config.fseApis() != null ? config.fseApis() : List.of();
        for (FseApiExport fseExport : fseApiExports) {
            ProviderEnvironment env = fseExport.environmentCode() != null
                    ? envByCode.get(fseExport.environmentCode())
                    : null;
            ProviderFseApi fseApi = fseApiRepository.save(ProviderFseApi.builder()
                    .provider(provider)
                    .environment(env)
                    .code(fseExport.code())
                    .nom(fseExport.nom())
                    .typeApi(fseExport.typeApi() != null ? fseExport.typeApi() : "REST")
                    .baseUrl(fseExport.baseUrl())
                    .host(fseExport.host())
                    .port(fseExport.port())
                    .useSsl(fseExport.useSsl())
                    .useProxy(fseExport.useProxy())
                    .actif(fseExport.actif())
                    .authType(fseExport.authType())
                    .clientId(fseExport.clientId())
                    .tokenUrl(fseExport.tokenUrl())
                    .customHeaderName(fseExport.customHeaderName())
                    .extraConfigJson(fseExport.extraConfigJson())
                    .build());
            fseApiByCode.put(fseApi.getCode(), fseApi);
        }

        Map<String, ProviderOperation> opByCode = new HashMap<>();
        for (OperationExport opExport : config.operations()) {
            ProviderOperation op = operationRepository.save(ProviderOperation.builder()
                    .provider(provider)
                    .code(opExport.code())
                    .nom(opExport.nom())
                    .description(opExport.description())
                    .idempotent(opExport.idempotent())
                    .actif(opExport.actif())
                    .build());
            opByCode.put(op.getCode(), op);
        }

        for (ApiExport apiExport : config.apis()) {
            ProviderOperation op = opByCode.get(apiExport.operationCode());
            if (op == null) {
                continue;
            }
            ProviderEnvironment env = apiExport.environmentCode() != null
                    ? envByCode.get(apiExport.environmentCode())
                    : null;
            ProviderFseApi fseApi = apiExport.fseApiCode() != null
                    ? fseApiByCode.get(apiExport.fseApiCode())
                    : null;
            apiRepository.save(ProviderApi.builder()
                    .provider(provider)
                    .environment(env)
                    .fseApi(fseApi)
                    .operation(op)
                    .code(apiExport.code())
                    .nom(apiExport.nom())
                    .httpMethod(apiExport.httpMethod())
                    .path(apiExport.path())
                    .contentType(apiExport.contentType())
                    .timeoutSeconds(apiExport.timeoutSeconds())
                    .requestMappingJson(apiExport.requestMappingJson())
                    .responseMappingJson(apiExport.responseMappingJson())
                    .retryEnabled(apiExport.retryEnabled())
                    .maxAttempts(apiExport.maxAttempts())
                    .retryDelayMs(apiExport.retryDelayMs())
                    .backoffMultiplier(apiExport.backoffMultiplier())
                    .retryOn(apiExport.retryOn())
                    .actif(apiExport.actif())
                    .build());
        }

        for (FlowExport flowExport : config.flows()) {
            ServiceTypeCode serviceType = flowExport.serviceType() != null
                    ? flowExport.serviceType()
                    : ServiceTypeCode.fromString(flowExport.code());
            if (serviceType == null) {
                serviceType = ServiceTypeCode.PAYEMENT;
            }
            PaymentFlow flow = flowRepository.save(PaymentFlow.builder()
                    .provider(provider)
                    .code(flowExport.code())
                    .nom(flowExport.nom())
                    .serviceType(serviceType)
                    .bpmProcessKey(flowExport.bpmProcessKey())
                    .description(flowExport.description())
                    .actif(flowExport.actif())
                    .build());
            for (FlowStepExport stepExport : flowExport.steps()) {
                stepRepository.save(PaymentFlowStep.builder()
                        .flow(flow)
                        .code(stepExport.code())
                        .stepType(stepExport.stepType())
                        .ordre(stepExport.ordre())
                        .apiOperationCode(stepExport.apiOperationCode())
                        .nextStepSuccess(stepExport.nextStepSuccess())
                        .nextStepFailure(stepExport.nextStepFailure())
                        .configurationJson(stepExport.configurationJson())
                        .actif(stepExport.actif())
                        .build());
            }
        }

        for (StatusMappingExport mappingExport : config.statusMappings()) {
            statusMappingRepository.save(ProviderStatusMapping.builder()
                    .provider(provider)
                    .providerStatus(mappingExport.providerStatus())
                    .gatewayStatus(mappingExport.gatewayStatus())
                    .messages(LocalizedMessages.normalize(mappingExport.messages()))
                    .build());
        }

        if (config.otpConfig() != null) {
            OtpConfigExport otp = config.otpConfig();
            otpConfigRepository.save(ProviderOtpConfig.builder()
                    .provider(provider)
                    .otpRequired(otp.otpRequired())
                    .length(otp.length())
                    .validitySeconds(otp.validitySeconds())
                    .maxAttempts(otp.maxAttempts())
                    .resendAllowed(otp.resendAllowed())
                    .resendDelaySeconds(otp.resendDelaySeconds())
                    .generateOperation(otp.generateOperation())
                    .validateOperation(otp.validateOperation())
                    .build());
        }

        for (WebhookExport webhookExport : config.webhooks()) {
            webhookConfigRepository.save(ProviderWebhookConfig.builder()
                    .provider(provider)
                    .pathSuffix(webhookExport.pathSuffix())
                    .authMode(webhookExport.authMode())
                    .allowedIps(webhookExport.allowedIps())
                    .actif(webhookExport.actif())
                    .build());
        }

        auditService.record(actor, "PROVIDER_IMPORT", "PROVIDER", provider.getId().toString(),
                "Import configuration " + provider.getCode());

        return providerService.getById(provider.getId());
    }

    private void applyProvider(PaymentProvider provider, ProviderExport export) {
        provider.setCode(export.code());
        provider.setLibelle(export.libelle());
        provider.setDescription(export.description());
        provider.setLogoUrl(export.logoUrl());
        provider.setActif(export.actif());
        provider.setOrdreAffichage(export.ordreAffichage());
        provider.setPaymentMethodType(export.paymentMethodType() != null
                ? export.paymentMethodType()
                : com.gateway.payment.common.domain.PaymentMethodType.MOBILE_MONEY);
        provider.setModeExecution(export.modeExecution());
        provider.setCodeFse(export.codeFse());
        provider.setCodeExterne(export.codeExterne());
        provider.setGroupCode(export.groupCode());
        provider.setCountryCode(export.countryCode());
        provider.setParticipantKind(export.participantKind());
        provider.setFseStatut(export.fseStatut());
        provider.getServiceTypes().clear();
        provider.getServiceTypes().addAll(serviceTypeService.resolveEntities(export.serviceTypes()));
    }

    private void clearProviderConfig(UUID providerId) {
        flowRepository.findByProviderId(providerId).forEach(flow ->
                stepRepository.findByFlowIdOrderByOrdreAsc(flow.getId()).forEach(stepRepository::delete));
        flowRepository.findByProviderId(providerId).forEach(flowRepository::delete);
        apiRepository.findByProviderId(providerId).forEach(apiRepository::delete);
        fseApiRepository.findByProviderId(providerId).forEach(fseApiRepository::delete);
        operationRepository.findByProviderId(providerId).forEach(operationRepository::delete);
        environmentRepository.findByProviderId(providerId).forEach(env ->
                authenticationRepository.findByProviderIdAndEnvironmentId(providerId, env.getId())
                        .ifPresent(authenticationRepository::delete));
        environmentRepository.findByProviderId(providerId).forEach(environmentRepository::delete);
        statusMappingRepository.findByProviderId(providerId).forEach(statusMappingRepository::delete);
        otpConfigRepository.findByProviderId(providerId).ifPresent(otpConfigRepository::delete);
        webhookConfigRepository.findByProviderId(providerId).forEach(webhookConfigRepository::delete);
    }
}
