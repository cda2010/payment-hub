package com.gateway.payment.configuration.api.dto;

import java.util.UUID;

public record OmnicanalMethodCreateResponse(
        UUID operationId,
        String operationCode,
        UUID endpointId,
        String endpointCode,
        String path,
        String httpMethod,
        UUID fseApiId,
        String fseApiCode,
        UUID environmentId,
        String environmentCode,
        String requestMappingJson
) {}
