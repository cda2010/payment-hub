package com.gateway.payment.configuration.api.dto;



import com.gateway.payment.common.domain.AuthType;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;



import java.util.UUID;



public record FseApiRequest(

        @NotBlank @Size(max = 50) String code,

        @NotBlank @Size(max = 150) String nom,

        @Size(max = 20) String typeApi,

        @NotBlank @Size(max = 500) String baseUrl,

        @Size(max = 255) String host,

        @Size(max = 20) String port,

        Boolean useSsl,

        Boolean useProxy,

        UUID environmentId,

        boolean actif,

        AuthType authType,

        String clientId,

        String clientSecret,

        String tokenUrl,

        String grantType,

        String username,

        String password,

        String apiKey,

        String customHeaderName,

        String customHeaderValue

) {}


