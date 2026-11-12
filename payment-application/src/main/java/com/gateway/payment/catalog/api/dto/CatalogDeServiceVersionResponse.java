package com.gateway.payment.catalog.api.dto;

import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CatalogDeServiceVersionResponse(
        UUID id,
        UUID catalogDeServiceId,
        String catalogDeServiceCode,
        String version,
        CatalogDeServiceStatus status,
        Instant effectiveFrom,
        Instant effectiveTo,
        String workflowKey,
        Map<String, Object> configuration,
        /** Traductions des libellés de {@code configuration}, indexées par chemin. */
        Map<String, Map<String, String>> translations,
        Instant createdAt,
        Instant updatedAt
) {}
