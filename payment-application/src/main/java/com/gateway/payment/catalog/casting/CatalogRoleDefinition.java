package com.gateway.payment.catalog.casting;

/**
 * Rôle déclaré sur une version produit catalogue ({@code configuration.roles[]}).
 */
public record CatalogRoleDefinition(
        String code,
        String label,
        CatalogRoleCardinality cardinality,
        CatalogRoleSelection selection,
        /** Rôle source si {@link CatalogRoleSelection#DERIVED}. */
        String derivedFrom
) {}
