package com.gateway.payment.billing.api.dto;

import java.util.UUID;

public record BillerResponse(
        UUID id,
        String code,
        String name,
        String description,
        String logoUrl,
        boolean actif,
        String defaultCurrency,
        Integer ordreAffichage,
        /** Code catégorie ({@code ENERGY_WATER}, …). */
        String categoryCode,
        /** Libellé affichable de la catégorie. */
        String categoryLabel,
        int productCount,
        UUID providerId,
        String providerCode,
        String providerLibelle
) {}
