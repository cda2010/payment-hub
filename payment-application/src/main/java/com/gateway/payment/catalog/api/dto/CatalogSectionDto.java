package com.gateway.payment.catalog.api.dto;

import com.gateway.payment.datasources.api.CatalogItemDto;

import java.util.List;

/** Section catalogue avec displayKind + items matérialisés. */
public record CatalogSectionDto(
        String id,
        String displayKind,
        String dataSource,
        String origin,
        String title,
        String bindsTo,
        boolean searchable,
        boolean pageable,
        boolean hasMore,
        boolean loading,
        String selectedId,
        List<CatalogItemDto> items
) {}
