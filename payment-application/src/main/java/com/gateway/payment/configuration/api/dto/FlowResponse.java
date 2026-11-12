package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FlowResponse(
        UUID id,
        UUID providerId,
        String code,
        String nom,
        ServiceTypeCode serviceType,
        String bpmProcessKey,
        String description,
        boolean actif,
        Instant createdAt,
        List<FlowStepResponse> steps
) {}
