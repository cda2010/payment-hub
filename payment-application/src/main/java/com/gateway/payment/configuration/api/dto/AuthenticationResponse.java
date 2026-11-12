package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.AuthType;

import java.time.Instant;
import java.util.UUID;

public record AuthenticationResponse(
        UUID id,
        UUID providerId,
        UUID environmentId,
        AuthType authType,
        String clientId,
        String tokenUrl,
        String customHeaderName,
        String extraConfigJson,
        boolean hasClientSecret,
        boolean hasApiKey,
        boolean hasUsername,
        boolean hasPassword,
        boolean hasCustomHeaderValue,
        Instant createdAt,
        Instant updatedAt
) {}
