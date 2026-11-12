package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchemaMatchRequest(
        @NotNull ServiceTypeCode serviceType,
        @NotBlank String sourceCodeFse,
        @NotBlank String destCodeFse,
        Integer siege
) {}
