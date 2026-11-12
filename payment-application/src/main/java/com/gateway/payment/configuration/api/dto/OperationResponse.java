package com.gateway.payment.configuration.api.dto;

import java.time.Instant;
import java.util.UUID;

public record OperationResponse(
        UUID id,
        UUID providerId,
        String code,
        String nom,
        String description,
        boolean idempotent,
        boolean actif,
        Instant createdAt
) {}
