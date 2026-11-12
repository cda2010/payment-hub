package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SchemaEcritureResponse(
        UUID id,
        String nomSchema,
        ServiceTypeCode serviceType,
        String patternCode,
        String workflowName,
        Integer workflowVersion,
        String engine,
        String errorPolicyCode,
        boolean actif,
        Instant createdAt,
        List<LigneSchemaEcritureResponse> lignes
) {}
