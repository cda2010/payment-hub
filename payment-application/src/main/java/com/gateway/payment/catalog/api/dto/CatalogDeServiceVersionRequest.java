package com.gateway.payment.catalog.api.dto;

import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

/**
 * Création d'une nouvelle version (toute modification importante).
 * Ne modifie jamais une version déjà PUBLISHED.
 */
public record CatalogDeServiceVersionRequest(
        @NotBlank @Size(max = 20) String version,
        @NotNull CatalogDeServiceStatus status,
        Instant effectiveFrom,
        Instant effectiveTo,
        @Size(max = 120) String workflowKey,
        Map<String, Object> configuration,
        /**
         * Traductions des libellés de {@code configuration} :
         * {@code {"configuration.roles[SOURCE].label": {"fr":"Compte émetteur","en":"Source account"}}}.
         */
        Map<String, Map<String, String>> translations
) {}
