package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.AuthType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuthenticationRequest(
        @NotNull UUID environmentId,
        @NotNull AuthType authType,
        String clientId,
        String clientSecret,
        String apiKey,
        String tokenUrl,
        String username,
        String password,
        String customHeaderName,
        String customHeaderValue,
        String extraConfigJson
) {}
