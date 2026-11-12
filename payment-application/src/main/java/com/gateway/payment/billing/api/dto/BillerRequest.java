package com.gateway.payment.billing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BillerRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        String description,
        String logoUrl,
        @NotNull Boolean actif,
        @Size(max = 10) String defaultCurrency,
        Integer ordreAffichage,
        /** Catégorie navigation ({@code ENERGY_WATER}, {@code TELECOM_MEDIA}, …). */
        @Size(max = 40) String categoryCode,
        /** Fournisseur de type FACTURIER (obligatoire). */
        @NotNull UUID providerId
) {}
