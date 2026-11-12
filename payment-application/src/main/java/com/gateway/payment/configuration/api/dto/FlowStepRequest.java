package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.StepType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FlowStepRequest(
        @NotBlank @Size(max = 50) String code,
        @NotNull StepType stepType,
        @NotNull Integer ordre,
        @Size(max = 50) String apiOperationCode,
        @Size(max = 50) String nextStepSuccess,
        @Size(max = 50) String nextStepFailure,
        String configurationJson,
        boolean actif
) {}
