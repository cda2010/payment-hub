package com.gateway.payment.datasources;

import java.util.List;
import java.util.Map;

/**
 * Section source de données découplée de la présentation catalogue
 * ({@code displayKind}, titres, …).
 */
public record DataSourceSection(
        String id,
        String dataSourceCode,
        CatalogDataSource dataSource,
        CatalogDataOrigin origin,
        Map<String, Object> params,
        List<StaticCatalogItem> staticItems,
        Integer pageSize
) {
    public DataSourceSection {
        params = params != null ? Map.copyOf(params) : Map.of();
        staticItems = staticItems != null ? List.copyOf(staticItems) : List.of();
    }
}
