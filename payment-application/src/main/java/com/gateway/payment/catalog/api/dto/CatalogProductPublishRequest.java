package com.gateway.payment.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Publish one-shot : produit + version + (optionnel) hôte payment_flow.
 */
public record CatalogProductPublishRequest(
        @NotBlank String code,
        /** Libellé dans la langue par défaut ({@code gateway.i18n.default-language}). */
        @NotBlank String name,
        String description,
        Map<String, String> nameI18n,
        Map<String, String> descriptionI18n,
        @NotBlank String categoryCode,
        String version,
        @NotBlank String workflowKey,
        Map<String, Object> configuration,
        /** Traductions des libellés de {@code configuration}, indexées par chemin. */
        Map<String, Map<String, String>> translations,
        /** Si true (défaut), crée/aligne provider+flow pour {@code workflowKey}. */
        Boolean ensureHostFlow,
        String hostProviderCode,
        String hostParticipantKind
) {}
