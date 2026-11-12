package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SchemaExecuteRequest(
        @NotNull ServiceTypeCode serviceType,
        @NotBlank String sourceCodeFse,
        @NotBlank String destCodeFse,
        UUID schemaEcritureId,
        String paymentId,
        Integer siege,
        Map<String, String> userParams
) {}
