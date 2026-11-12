package com.gateway.payment.catalog.api.dto;

import java.util.List;

/** Layout catalogue produit (composition + données). */
public record CatalogLayoutResponse(
        int version,
        String productCode,
        String journeyCode,
        String channel,
        List<CatalogSectionDto> sections
) {}
