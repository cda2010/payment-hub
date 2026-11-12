package com.gateway.payment.catalog.casting;

/**
 * Provider éligible pour un rôle ({@code configuration.participants[]}).
 */
public record CatalogParticipantDefinition(
        String role,
        String providerCode,
        boolean defaultParticipant
) {}
