package com.gateway.payment.configuration.api.dto;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentResponse(
        UUID id,
        UUID providerId,
        String code,
        String baseUrl,
        Integer timeoutSeconds,
        boolean actif,
        Instant createdAt,
        Instant updatedAt
) {}
