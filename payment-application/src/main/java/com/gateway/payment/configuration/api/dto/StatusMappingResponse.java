package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.PaymentStatus;

import java.util.Map;
import java.util.UUID;

public record StatusMappingResponse(
        UUID id,
        UUID providerId,
        String providerStatus,
        PaymentStatus gatewayStatus,
        Map<String, String> messages
) {}
