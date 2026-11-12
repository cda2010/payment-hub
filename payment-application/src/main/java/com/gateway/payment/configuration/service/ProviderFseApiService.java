package com.gateway.payment.configuration.service;



import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.gateway.payment.audit.service.AuditService;

import com.gateway.payment.common.crypto.SecretEncryptionService;

import com.gateway.payment.common.domain.AuthType;

import com.gateway.payment.common.exception.BusinessException;

import com.gateway.payment.configuration.api.dto.FseApiRequest;

import com.gateway.payment.configuration.api.dto.FseApiResponse;

import com.gateway.payment.configuration.domain.PaymentProvider;

import com.gateway.payment.configuration.domain.ProviderEnvironment;

import com.gateway.payment.configuration.domain.ProviderFseApi;

import com.gateway.payment.configuration.repository.ProviderApiRepository;

import com.gateway.payment.configuration.repository.ProviderFseApiRepository;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



import java.util.List;

import java.util.UUID;



@Service

public class ProviderFseApiService {



    private final ProviderFseApiRepository fseApiRepository;

    private final ProviderApiRepository apiRepository;

    private final ProviderService providerService;

    private final EnvironmentService environmentService;

    private final SecretEncryptionService encryptionService;

    private final AuditService auditService;

    private final ObjectMapper objectMapper;

    private final OmnicanalProviderCatalog omnicanalCatalog;



    public ProviderFseApiService(

            ProviderFseApiRepository fseApiRepository,

            ProviderApiRepository apiRepository,

            ProviderService providerService,

            EnvironmentService environmentService,

            SecretEncryptionService encryptionService,

            AuditService auditService,

            ObjectMapper objectMapper,

            OmnicanalProviderCatalog omnicanalCatalog

    ) {

        this.fseApiRepository = fseApiRepository;

        this.apiRepository = apiRepository;

        this.providerService = providerService;

        this.environmentService = environmentService;

        this.encryptionService = encryptionService;

        this.auditService = auditService;

        this.objectMapper = objectMapper;

        this.omnicanalCatalog = omnicanalCatalog;

    }



    @Transactional(readOnly = true)

    public List<FseApiResponse> listByProvider(UUID providerId) {

        List<FseApiResponse> remote = omnicanalCatalog.fseApis(providerId);
        if (remote != null) {
            return remote;
        }
        providerService.requireProvider(providerId);

        return fseApiRepository.findByProviderId(providerId).stream()

                .map(this::toResponse)

                .toList();

    }



    @Transactional(readOnly = true)

    public ProviderFseApi requireFseApi(UUID providerId, UUID fseApiId) {

        ProviderFseApi api = fseApiRepository.findById(fseApiId)

                .orElseThrow(() -> new BusinessException("FSE_API_NOT_FOUND", "Api introuvable", HttpStatus.NOT_FOUND));

        if (!api.getProvider().getId().equals(providerId)) {

            throw new BusinessException("FSE_API_NOT_FOUND", "Api introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);

        }

        return api;

    }



    @Transactional

    public FseApiResponse create(UUID providerId, FseApiRequest request, String actor) {

        PaymentProvider provider = providerService.requireProvider(providerId);

        String code = request.code().trim().toUpperCase();

        fseApiRepository.findByProviderIdAndCode(providerId, code).ifPresent(a -> {

            throw new BusinessException("FSE_API_CODE_EXISTS", "Code Api déjà utilisé");

        });



        ProviderEnvironment env = resolveEnvironment(providerId, request.environmentId());

        ProviderFseApi saved = fseApiRepository.save(ProviderFseApi.builder()

                .provider(provider)

                .environment(env)

                .code(code)

                .nom(request.nom().trim())

                .typeApi(StringUtils.hasText(request.typeApi()) ? request.typeApi().trim().toUpperCase() : "REST")

                .baseUrl(request.baseUrl().trim())

                .host(blankToNull(request.host()))

                .port(blankToNull(request.port()))

                .useSsl(Boolean.TRUE.equals(request.useSsl()))

                .useProxy(Boolean.TRUE.equals(request.useProxy()))

                .actif(request.actif())

                .build());

        applyAuth(saved, request);



        auditService.record(actor, "FSE_API_CREATE", "PROVIDER_FSE_API", saved.getId().toString(),

                "Création Api " + saved.getCode());

        return toResponse(fseApiRepository.save(saved));

    }



    @Transactional

    public FseApiResponse update(UUID providerId, UUID fseApiId, FseApiRequest request, String actor) {

        ProviderFseApi api = requireFseApi(providerId, fseApiId);

        String code = request.code().trim().toUpperCase();

        fseApiRepository.findByProviderIdAndCode(providerId, code)

                .filter(existing -> !existing.getId().equals(fseApiId))

                .ifPresent(a -> {

                    throw new BusinessException("FSE_API_CODE_EXISTS", "Code Api déjà utilisé");

                });



        api.setEnvironment(resolveEnvironment(providerId, request.environmentId()));

        api.setCode(code);

        api.setNom(request.nom().trim());

        api.setTypeApi(StringUtils.hasText(request.typeApi()) ? request.typeApi().trim().toUpperCase() : "REST");

        api.setBaseUrl(request.baseUrl().trim());

        api.setHost(blankToNull(request.host()));

        api.setPort(blankToNull(request.port()));

        api.setUseSsl(Boolean.TRUE.equals(request.useSsl()));

        api.setUseProxy(Boolean.TRUE.equals(request.useProxy()));

        api.setActif(request.actif());

        applyAuth(api, request);



        ProviderFseApi saved = fseApiRepository.save(api);

        auditService.record(actor, "FSE_API_UPDATE", "PROVIDER_FSE_API", fseApiId.toString(),

                "Mise à jour Api " + saved.getCode());

        return toResponse(saved);

    }



    private void applyAuth(ProviderFseApi api, FseApiRequest request) {

        AuthType authType = request.authType() != null ? request.authType() : AuthType.NONE;

        api.setAuthType(authType);

        api.setClientId(blankToNull(request.clientId()));

        api.setTokenUrl(blankToNull(request.tokenUrl()));

        api.setCustomHeaderName(blankToNull(request.customHeaderName()));



        if (StringUtils.hasText(request.grantType())) {

            api.setExtraConfigJson("{\"grantType\":\"" + request.grantType().trim().replace("\"", "") + "\"}");

        } else if (authType == AuthType.NONE) {

            api.setExtraConfigJson(null);

        }



        if (StringUtils.hasText(request.clientSecret())) {

            api.setClientSecretEnc(encryptionService.encrypt(request.clientSecret()));

        }

        if (StringUtils.hasText(request.apiKey())) {

            api.setApiKeyEnc(encryptionService.encrypt(request.apiKey()));

        }

        if (StringUtils.hasText(request.username())) {

            api.setUsernameEnc(encryptionService.encrypt(request.username()));

        }

        if (StringUtils.hasText(request.password())) {

            api.setPasswordEnc(encryptionService.encrypt(request.password()));

        }

        if (StringUtils.hasText(request.customHeaderValue())) {

            api.setCustomHeaderValueEnc(encryptionService.encrypt(request.customHeaderValue()));

        }

    }



    private ProviderEnvironment resolveEnvironment(UUID providerId, UUID environmentId) {

        if (environmentId == null) {

            return null;

        }

        return environmentService.requireEnvironment(providerId, environmentId);

    }



    private FseApiResponse toResponse(ProviderFseApi api) {

        int methods = (int) apiRepository.countByFseApiId(api.getId());

        return new FseApiResponse(

                api.getId(),

                api.getProvider().getId(),

                api.getEnvironment() != null ? api.getEnvironment().getId() : null,

                api.getEnvironment() != null ? api.getEnvironment().getCode() : null,

                api.getCode(),

                api.getNom(),

                api.getTypeApi(),

                api.getBaseUrl(),

                api.getHost(),

                api.getPort(),

                api.isUseSsl(),

                api.isUseProxy(),

                api.isActif(),

                methods,

                api.getAuthType() != null ? api.getAuthType() : AuthType.NONE,

                api.getClientId(),

                api.getTokenUrl(),

                extractGrantType(api.getExtraConfigJson()),

                api.getCustomHeaderName(),

                StringUtils.hasText(api.getClientSecretEnc()),

                StringUtils.hasText(api.getApiKeyEnc()),

                StringUtils.hasText(api.getUsernameEnc()),

                StringUtils.hasText(api.getPasswordEnc()),

                StringUtils.hasText(api.getCustomHeaderValueEnc()),

                api.getCreatedAt()

        );

    }



    private String extractGrantType(String extraConfigJson) {

        if (!StringUtils.hasText(extraConfigJson)) {

            return null;

        }

        try {

            JsonNode node = objectMapper.readTree(extraConfigJson);

            JsonNode grant = node.get("grantType");

            return grant != null && !grant.isNull() ? grant.asText() : null;

        } catch (Exception ex) {

            return null;

        }

    }



    private static String blankToNull(String v) {

        return StringUtils.hasText(v) ? v.trim() : null;

    }

}


