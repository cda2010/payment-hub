package com.gateway.payment.billing.api.dto;

/**
 * Catégorie facturier pour navigation mobile (dérivée des facturiers actifs).
 */
public record BillerCategoryResponse(
        String code,
        String label,
        int sortOrder,
        long billerCount
) {}
