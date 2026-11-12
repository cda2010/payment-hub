package com.gateway.payment.catalog.api.dto;

/**
 * Binding produit catalogue ↔ process RapidBPMN déployé.
 */
public record CatalogWorkflowBindingResponse(
        String workflowKey,
        String processName,
        Integer processVersion,
        String deploymentId,
        String productCode,
        String catalogVersion,
        boolean deployed
) {}
