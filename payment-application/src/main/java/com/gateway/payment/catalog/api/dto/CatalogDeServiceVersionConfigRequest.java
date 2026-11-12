package com.gateway.payment.catalog.api.dto;

import java.util.Map;

/**
 * Mise à jour de la configuration d'une version DRAFT / VALIDATED
 * (roles, participants, steps…). Interdit sur PUBLISHED / ARCHIVED.
 */
public record CatalogDeServiceVersionConfigRequest(
        Map<String, Object> configuration,
        /** Traductions des libellés de {@code configuration}, indexées par chemin. */
        Map<String, Map<String, String>> translations
) {}
