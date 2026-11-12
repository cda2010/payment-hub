package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvironmentRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank String baseUrl,
        Integer timeoutSeconds,
        boolean actif
) {}
