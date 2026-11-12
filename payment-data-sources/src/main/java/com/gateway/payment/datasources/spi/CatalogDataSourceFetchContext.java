package com.gateway.payment.datasources.spi;

import com.gateway.payment.datasources.DataSourceSection;

import java.util.Map;

/**
 * Contexte de résolution d'une source — section présentation ou appel API direct.
 */
public record CatalogDataSourceFetchContext(
        DataSourceSection section,
        Map<String, Object> configuration,
        String category,
        String q,
        int page,
        int size
) {
    public CatalogDataSourceFetchContext {
        configuration = configuration != null ? configuration : Map.of();
        category = blankToNull(category);
        q = blankToNull(q);
        page = Math.max(0, page);
        size = size > 0 ? size : 30;
    }

    private static String blankToNull(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        return v.trim();
    }
}
