package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.util.List;
import java.util.UUID;

public record SchemaExecuteResponse(
        UUID schemaId,
        String nomSchema,
        String patternCode,
        String workflowName,
        ServiceTypeCode serviceType,
        boolean success,
        String message,
        List<SchemaLegResult> legs
) {}
