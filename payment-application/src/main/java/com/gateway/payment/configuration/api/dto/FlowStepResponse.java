package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.StepType;

import java.util.UUID;

public record FlowStepResponse(
        UUID id,
        UUID flowId,
        String code,
        StepType stepType,
        Integer ordre,
        String apiOperationCode,
        String nextStepSuccess,
        String nextStepFailure,
        String configurationJson,
        boolean actif
) {}
