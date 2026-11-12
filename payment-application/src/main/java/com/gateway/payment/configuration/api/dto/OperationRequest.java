package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OperationRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String nom,
        String description,
        boolean idempotent,
        boolean actif
) {}
