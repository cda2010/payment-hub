package com.gateway.payment.configuration.api.dto;

import java.util.UUID;

public record WebhookConfigResponse(
        UUID id,
        UUID providerId,
        String pathSuffix,
        String authMode,
        boolean hasHmacSecret,
        boolean hasApiKey,
        String allowedIps,
        boolean actif
) {}
