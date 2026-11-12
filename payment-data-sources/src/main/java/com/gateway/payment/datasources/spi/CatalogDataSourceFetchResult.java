package com.gateway.payment.datasources.spi;

import com.gateway.payment.datasources.api.CatalogItemDto;

import java.util.List;

/** Résultat d'hydratation d'une source catalogue. */
public record CatalogDataSourceFetchResult(List<CatalogItemDto> items, boolean hasMore) {

    public static CatalogDataSourceFetchResult empty() {
        return new CatalogDataSourceFetchResult(List.of(), false);
    }

    public CatalogDataSourceFetchResult {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
