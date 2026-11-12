package com.gateway.payment.catalog.api.dto;

import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record CatalogDeServiceRequest(
        @NotBlank @Size(max = 60) String code,
        /** Libellé dans la langue par défaut ({@code gateway.i18n.default-language}). */
        @NotBlank @Size(max = 150) String name,
        String description,
        /** Traductions du nom : {@code {"fr":"Paiement de facture","en":"Bill payment"}}. */
        Map<String, String> nameI18n,
        /** Traductions de la description. */
        Map<String, String> descriptionI18n,
        /** Code de {@link com.gateway.payment.catalog.domain.CategorieService} (ex. TRANSFER, SAVINGS). */
        @Size(max = 50) String categoryCode,
        /** Alternative à {@code categoryCode} si l'id est déjà connu. */
        UUID categoryId,
        @NotNull CatalogDeServiceStatus status,
        /** Version initiale à créer (défaut {@code 1.0}). */
        @Size(max = 20) String initialVersion,
        @Size(max = 120) String workflowKey,
        Map<String, Object> configuration,
        /** Traductions des libellés de {@code configuration}, indexées par chemin. */
        Map<String, Map<String, String>> translations
) {}
