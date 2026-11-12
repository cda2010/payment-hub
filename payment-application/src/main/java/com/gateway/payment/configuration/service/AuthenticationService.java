package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.AuthenticationRequest;
import com.gateway.payment.configuration.api.dto.AuthenticationResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderAuthentication;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.repository.ProviderAuthenticationRepository;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final ProviderAuthenticationRepository authenticationRepository;
    private final ProviderService providerService;
    private final EnvironmentService environmentService;
    private final SecretEncryptionService secretEncryptionService;
    private final AuditService auditService;
    private final OmnicanalProviderCatalog omnicanalCatalog;

    public AuthenticationService(
            ProviderAuthenticationRepository authenticationRepository,
            ProviderService providerService,
            EnvironmentService environmentService,
            SecretEncryptionService secretEncryptionService,
            AuditService auditService,
            OmnicanalProviderCatalog omnicanalCatalog
    ) {
        this.authenticationRepository = authenticationRepository;
        this.providerService = providerService;
        this.environmentService = environmentService;
        this.secretEncryptionService = secretEncryptionService;
        this.auditService = auditService;
        this.omnicanalCatalog = omnicanalCatalog;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse get(UUID providerId, UUID environmentId) {
        AuthenticationResponse remote = omnicanalCatalog.authentication(providerId, environmentId);
        if (remote != null) {
            return remote;
        }
        providerService.requireProvider(providerId);
        environmentService.requireEnvironment(providerId, environmentId);
        ProviderAuthentication auth = authenticationRepository
                .findByProviderIdAndEnvironmentId(providerId, environmentId)
                .orElseThrow(() -> new BusinessException("AUTH_NOT_FOUND",
                        "Authentification introuvable pour cet environnement", HttpStatus.NOT_FOUND));
        return toResponse(auth);
    }

    @Transactional
    public AuthenticationResponse upsert(UUID providerId, AuthenticationRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        ProviderEnvironment environment = environmentService.requireEnvironment(providerId, request.environmentId());

        ProviderAuthentication auth = authenticationRepository
                .findByProviderIdAndEnvironmentId(providerId, request.environmentId())
                .orElseGet(() -> ProviderAuthentication.builder()
                        .provider(provider)
                        .environment(environment)
                        .build());

        auth.setAuthType(request.authType());
        auth.setClientId(request.clientId());
        auth.setTokenUrl(request.tokenUrl());
        auth.setCustomHeaderName(request.customHeaderName());
        auth.setExtraConfigJson(request.extraConfigJson());

        if (StringUtils.hasText(request.clientSecret())) {
            auth.setClientSecretEnc(secretEncryptionService.encrypt(request.clientSecret()));
        }
        if (StringUtils.hasText(request.apiKey())) {
            auth.setApiKeyEnc(secretEncryptionService.encrypt(request.apiKey()));
        }
        if (StringUtils.hasText(request.username())) {
            auth.setUsernameEnc(secretEncryptionService.encrypt(request.username()));
        }
        if (StringUtils.hasText(request.password())) {
            auth.setPasswordEnc(secretEncryptionService.encrypt(request.password()));
        }
        if (StringUtils.hasText(request.customHeaderValue())) {
            auth.setCustomHeaderValueEnc(secretEncryptionService.encrypt(request.customHeaderValue()));
        }

        ProviderAuthentication saved = authenticationRepository.save(auth);
        auditService.record(actor, "AUTH_UPSERT", "AUTHENTICATION", saved.getId().toString(),
                "Upsert authentification provider=" + providerId + " env=" + request.environmentId());
        return toResponse(saved);
    }

    private AuthenticationResponse toResponse(ProviderAuthentication auth) {
        return new AuthenticationResponse(
                auth.getId(),
                auth.getProvider().getId(),
                auth.getEnvironment().getId(),
                auth.getAuthType(),
                auth.getClientId(),
                auth.getTokenUrl(),
                auth.getCustomHeaderName(),
                auth.getExtraConfigJson(),
                StringUtils.hasText(auth.getClientSecretEnc()),
                StringUtils.hasText(auth.getApiKeyEnc()),
                StringUtils.hasText(auth.getUsernameEnc()),
                StringUtils.hasText(auth.getPasswordEnc()),
                StringUtils.hasText(auth.getCustomHeaderValueEnc()),
                auth.getCreatedAt(),
                auth.getUpdatedAt()
        );
    }
}
