package com.gateway.payment.integration.api.dto;

import java.util.List;

/** Page d'historique des transactions gateway. */
public record GatewayTransactionPageResponse(
        List<GatewayTransactionSummary> items,
        int page,
        int size,
        long total,
        boolean hasMore
) {}
