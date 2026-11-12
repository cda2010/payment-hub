package com.gateway.payment.billing.api.dto;

import java.util.List;

/** Page de facturiers (filtre catégorie / recherche / pagination). */
public record BillerPageResponse(
        List<BillerResponse> items,
        int page,
        int size,
        long total,
        boolean hasMore
) {}
