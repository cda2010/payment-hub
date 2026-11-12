package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookConfigRequest(
        @NotBlank @Size(max = 200) String pathSuffix,
        @Size(max = 50) String authMode,
        String hmacSecret,
        String apiKey,
        String allowedIps,
        boolean actif
) {}
