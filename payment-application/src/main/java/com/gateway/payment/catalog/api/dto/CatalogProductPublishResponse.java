package com.gateway.payment.catalog.api.dto;

public record CatalogProductPublishResponse(
        CatalogDeServiceResponse service,
        CatalogDeServiceVersionResponse version,
        CatalogHostFlowServiceView hostFlow
) {
    public record CatalogHostFlowServiceView(
            String providerCode,
            String workflowKey,
            String serviceType,
            boolean created
    ) {}
}
