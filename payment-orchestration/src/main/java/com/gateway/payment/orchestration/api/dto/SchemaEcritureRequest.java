package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SchemaEcritureRequest(
        @NotBlank String nomSchema,
        @NotNull ServiceTypeCode serviceType,
        @NotBlank String patternCode,
        String workflowName,
        Integer workflowVersion,
        String engine,
        String errorPolicyCode,
        boolean actif
) {}
