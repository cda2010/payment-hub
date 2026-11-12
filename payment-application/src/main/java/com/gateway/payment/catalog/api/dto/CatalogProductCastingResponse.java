package com.gateway.payment.catalog.api.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Config casting d'un produit PUBLISHED (roles / participants / bindings / journey)
 * pour l'UI publique (transfert, checkout, cash-out…).
 */
public record CatalogProductCastingResponse(
        String productCode,
        UUID versionId,
        String version,
        String workflowKey,
        List<Map<String, Object>> roles,
        List<Map<String, Object>> participants,
        List<Map<String, Object>> bindings,
        String journeyCode,
        List<String> supportedChannels,
        String defaultChannel,
        List<String> supportedAudiences,
        String defaultAudience
) {}
