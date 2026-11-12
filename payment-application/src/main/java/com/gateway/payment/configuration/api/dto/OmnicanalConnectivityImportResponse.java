package com.gateway.payment.configuration.api.dto;

import java.util.List;
import java.util.UUID;

public record OmnicanalConnectivityImportResponse(
        UUID providerId,
        String providerCode,
        String environmentCode,
        UUID environmentId,
        int operationsUpserted,
        int apisUpserted,
        boolean authenticationUpserted,
        List<String> operationCodes
) {}
