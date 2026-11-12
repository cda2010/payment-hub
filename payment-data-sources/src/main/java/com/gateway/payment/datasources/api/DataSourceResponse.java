package com.gateway.payment.datasources.api;

import java.util.List;
import java.util.Map;

/**
 * Réponse normalisée d'une source de données (Formio / catalogue / mobile).
 */
public record DataSourceResponse(
        String code,
        String origin,
        List<CatalogItemDto> items,
        boolean hasMore,
        Map<String, Object> params
) {}
