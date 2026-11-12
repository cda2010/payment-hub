package com.gateway.payment.transfer.api.dto;

import java.util.List;
import java.util.UUID;

public record FeeRuleResponse(
        UUID id,
        String code,
        String name,
        String currency,
        String description,
        boolean actif,
        com.gateway.payment.transfer.domain.FeeBearer feeBearer,
        List<FeeRuleLineDto> lines
) {}

