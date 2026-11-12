package com.gateway.payment.configuration.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiResponse(
        UUID id,
        UUID providerId,
        UUID fseApiId,
        String fseApiCode,
        UUID environmentId,
        UUID operationId,
        String code,
        String nom,
        String httpMethod,
        String path,
        String contentType,
        Integer timeoutSeconds,
        String requestMappingJson,
        String responseMappingJson,
        boolean retryEnabled,
        Integer maxAttempts,
        Long retryDelayMs,
        Double backoffMultiplier,
        String retryOn,
        boolean actif,
        Instant createdAt
) {}
