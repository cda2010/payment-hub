package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.util.List;
import java.util.UUID;

public record SchemaMatchResponse(
        UUID schemaId,
        String nomSchema,
        String patternCode,
        String workflowName,
        Integer workflowVersion,
        ServiceTypeCode serviceType,
        List<LigneSchemaEcritureResponse> lignes
) {}
