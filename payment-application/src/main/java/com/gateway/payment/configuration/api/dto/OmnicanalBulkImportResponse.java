package com.gateway.payment.configuration.api.dto;

import java.util.List;
import java.util.UUID;

public record OmnicanalBulkImportResponse(
        int fixturesProcessed,
        int providersCreated,
        int connectivityImports,
        int failures,
        List<Item> items
) {
    public record Item(
            String codeFse,
            String providerCode,
            UUID providerId,
            boolean providerCreated,
            int operationsUpserted,
            int apisUpserted,
            String error
    ) {}
}
