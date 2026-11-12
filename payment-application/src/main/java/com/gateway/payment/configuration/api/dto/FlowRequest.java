package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FlowRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String nom,
        @NotNull ServiceTypeCode serviceType,
        @Size(max = 100) String bpmProcessKey,
        String description,
        boolean actif
) {}
