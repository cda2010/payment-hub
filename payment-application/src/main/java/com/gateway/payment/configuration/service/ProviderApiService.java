package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.ApiRequest;
import com.gateway.payment.configuration.api.dto.ApiResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProviderApiService {

    private final ProviderApiRepository apiRepository;
    private final ProviderService providerService;
    private final EnvironmentService environmentService;
    private final OperationService operationService;
    private final AuditService auditService;
    private final OmnicanalProviderCatalog omnicanalCatalog;

    public ProviderApiService(
            ProviderApiRepository apiRepository,
            ProviderService providerService,
            EnvironmentService environmentService,
            OperationService operationService,
            AuditService auditService,
            OmnicanalProviderCatalog omnicanalCatalog
    ) {
        this.apiRepository = apiRepository;
        this.providerService = providerService;
        this.environmentService = environmentService;
        this.operationService = operationService;
        this.auditService = auditService;
        this.omnicanalCatalog = omnicanalCatalog;
    }

    @Transactional(readOnly = true)
    public List<ApiResponse> listByProvider(UUID providerId) {
        List<ApiResponse> remote = omnicanalCatalog.methods(providerId);
        if (remote != null) {
            return remote;
        }
        providerService.requireProvider(providerId);
        return apiRepository.findByProviderId(providerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApiResponse> listByFseApi(UUID providerId, UUID fseApiId) {
        List<ApiResponse> remote = omnicanalCatalog.methodsOf(providerId, fseApiId);
        if (remote != null) {
            return remote;
        }
        providerService.requireProvider(providerId);
        return apiRepository.findByFseApiId(fseApiId).stream()
                .filter(api -> api.getProvider().getId().equals(providerId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApiResponse create(UUID providerId, ApiRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        ProviderOperation operation = operationService.requireOperation(providerId, request.operationId());
        ProviderEnvironment environment = resolveEnvironment(providerId, request.environmentId());

        apiRepository.findByProviderIdAndCode(providerId, request.code()).ifPresent(a -> {
            throw new BusinessException("API_CODE_EXISTS", "Code API déjà utilisé");
        });

        ProviderApi api = ProviderApi.builder()
                .provider(provider)
                .environment(environment)
                .operation(operation)
                .code(request.code())
                .nom(request.nom())
                .httpMethod(request.httpMethod())
                .path(request.path())
                .contentType(request.contentType())
                .timeoutSeconds(request.timeoutSeconds())
                .requestMappingJson(request.requestMappingJson())
                .responseMappingJson(request.responseMappingJson())
                .retryEnabled(request.retryEnabled())
                .maxAttempts(request.maxAttempts())
                .retryDelayMs(request.retryDelayMs())
                .backoffMultiplier(request.backoffMultiplier())
                .retryOn(request.retryOn())
                .actif(request.actif())
                .build();
        ProviderApi saved = apiRepository.save(api);
        auditService.record(actor, "API_CREATE", "PROVIDER_API", saved.getId().toString(),
                "Création API " + saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public ApiResponse update(UUID providerId, UUID apiId, ApiRequest request, String actor) {
        ProviderApi api = requireApi(providerId, apiId);
        ProviderOperation operation = operationService.requireOperation(providerId, request.operationId());
        ProviderEnvironment environment = resolveEnvironment(providerId, request.environmentId());

        apiRepository.findByProviderIdAndCode(providerId, request.code())
                .filter(existing -> !existing.getId().equals(apiId))
                .ifPresent(a -> {
                    throw new BusinessException("API_CODE_EXISTS", "Code API déjà utilisé");
                });

        api.setEnvironment(environment);
        api.setOperation(operation);
        api.setCode(request.code());
        api.setNom(request.nom());
        api.setHttpMethod(request.httpMethod());
        api.setPath(request.path());
        api.setContentType(request.contentType());
        api.setTimeoutSeconds(request.timeoutSeconds());
        api.setRequestMappingJson(request.requestMappingJson());
        api.setResponseMappingJson(request.responseMappingJson());
        api.setRetryEnabled(request.retryEnabled());
        api.setMaxAttempts(request.maxAttempts());
        api.setRetryDelayMs(request.retryDelayMs());
        api.setBackoffMultiplier(request.backoffMultiplier());
        api.setRetryOn(request.retryOn());
        api.setActif(request.actif());

        ProviderApi saved = apiRepository.save(api);
        auditService.record(actor, "API_UPDATE", "PROVIDER_API", apiId.toString(),
                "Mise à jour API " + saved.getCode());
        return toResponse(saved);
    }

    private ProviderEnvironment resolveEnvironment(UUID providerId, UUID environmentId) {
        if (environmentId == null) {
            return null;
        }
        return environmentService.requireEnvironment(providerId, environmentId);
    }

    private ProviderApi requireApi(UUID providerId, UUID apiId) {
        ProviderApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new BusinessException("API_NOT_FOUND", "API introuvable", HttpStatus.NOT_FOUND));
        if (!api.getProvider().getId().equals(providerId)) {
            throw new BusinessException("API_NOT_FOUND", "API introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);
        }
        return api;
    }

    private ApiResponse toResponse(ProviderApi api) {
        return new ApiResponse(
                api.getId(),
                api.getProvider().getId(),
                api.getFseApi() != null ? api.getFseApi().getId() : null,
                api.getFseApi() != null ? api.getFseApi().getCode() : null,
                api.getEnvironment() != null ? api.getEnvironment().getId() : null,
                api.getOperation().getId(),
                api.getCode(),
                api.getNom(),
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
                api.isActif(),
                api.getCreatedAt()
        );
    }
}
