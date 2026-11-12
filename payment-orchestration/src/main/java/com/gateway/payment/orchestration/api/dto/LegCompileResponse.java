package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ProprieteApi;

import java.util.Map;
import java.util.UUID;

public record LegCompileResponse(
        String codeFse,
        String typeMethode,
        ProprieteApi apiAUtiliser,
        UUID providerId,
        UUID providerOperationId,
        String providerOperationCode,
        UUID providerApiId,
        String providerApiPath,
        String httpMethod,
        Map<String, String> resolvedParams
) {}
