package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApiRequest(
        UUID environmentId,
        @NotNull UUID operationId,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String nom,
        @NotBlank @Size(max = 20) String httpMethod,
        @NotBlank String path,
        @Size(max = 100) String contentType,
        Integer timeoutSeconds,
        String requestMappingJson,
        String responseMappingJson,
        boolean retryEnabled,
        Integer maxAttempts,
        Long retryDelayMs,
        Double backoffMultiplier,
        String retryOn,
        boolean actif
) {}
