package com.gateway.payment.configuration.api.dto;



import com.gateway.payment.common.domain.AuthType;



import java.time.Instant;

import java.util.UUID;



public record FseApiResponse(

        UUID id,

        UUID providerId,

        UUID environmentId,

        String environmentCode,

        String code,

        String nom,

        String typeApi,

        String baseUrl,

        String host,

        String port,

        boolean useSsl,

        boolean useProxy,

        boolean actif,

        int methodCount,

        AuthType authType,

        String clientId,

        String tokenUrl,

        String grantType,

        String customHeaderName,

        boolean hasClientSecret,

        boolean hasApiKey,

        boolean hasUsername,

        boolean hasPassword,

        boolean hasCustomHeaderValue,

        Instant createdAt

) {}


