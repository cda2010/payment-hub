package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.configuration.api.dto.WebhookConfigRequest;
import com.gateway.payment.configuration.api.dto.WebhookConfigResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderWebhookConfig;
import com.gateway.payment.configuration.repository.ProviderWebhookConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class WebhookConfigService {

    private final ProviderWebhookConfigRepository webhookConfigRepository;
    private final ProviderService providerService;
    private final SecretEncryptionService secretEncryptionService;
    private final AuditService auditService;

    public WebhookConfigService(
            ProviderWebhookConfigRepository webhookConfigRepository,
            ProviderService providerService,
            SecretEncryptionService secretEncryptionService,
            AuditService auditService
    ) {
        this.webhookConfigRepository = webhookConfigRepository;
        this.providerService = providerService;
        this.secretEncryptionService = secretEncryptionService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WebhookConfigResponse> listByProvider(UUID providerId) {
        providerService.requireProvider(providerId);
        return webhookConfigRepository.findByProviderId(providerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WebhookConfigResponse upsert(UUID providerId, WebhookConfigRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);

        ProviderWebhookConfig config = webhookConfigRepository.findByProviderId(providerId).stream()
                .filter(w -> w.getPathSuffix().equals(request.pathSuffix()))
                .findFirst()
                .orElseGet(() -> webhookConfigRepository.findByProviderId(providerId).stream()
                        .findFirst()
                        .orElseGet(() -> ProviderWebhookConfig.builder().provider(provider).build()));

        config.setProvider(provider);
        config.setPathSuffix(request.pathSuffix());
        config.setAuthMode(request.authMode());
        config.setAllowedIps(request.allowedIps());
        config.setActif(request.actif());

        if (StringUtils.hasText(request.hmacSecret())) {
            config.setHmacSecretEnc(secretEncryptionService.encrypt(request.hmacSecret()));
        }
        if (StringUtils.hasText(request.apiKey())) {
            config.setApiKeyEnc(secretEncryptionService.encrypt(request.apiKey()));
        }

        ProviderWebhookConfig saved = webhookConfigRepository.save(config);
        auditService.record(actor, "WEBHOOK_CONFIG_UPSERT", "WEBHOOK_CONFIG", saved.getId().toString(),
                "Upsert webhook " + saved.getPathSuffix());
        return toResponse(saved);
    }

    private WebhookConfigResponse toResponse(ProviderWebhookConfig config) {
        return new WebhookConfigResponse(
                config.getId(),
                config.getProvider().getId(),
                config.getPathSuffix(),
                config.getAuthMode(),
                StringUtils.hasText(config.getHmacSecretEnc()),
                StringUtils.hasText(config.getApiKeyEnc()),
                config.getAllowedIps(),
                config.isActif()
        );
    }
}
