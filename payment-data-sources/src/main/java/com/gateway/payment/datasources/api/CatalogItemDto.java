package com.gateway.payment.datasources.api;

import java.util.Map;

/** Item catalogue normalisé — indépendant du métier. */
public record CatalogItemDto(
        String id,
        String label,
        String sub,
        String icon,
        String value,
        Map<String, Object> meta
) {}
