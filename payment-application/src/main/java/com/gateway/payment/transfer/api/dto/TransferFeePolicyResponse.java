package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.FeePolicyType;

import java.util.UUID;

public record TransferFeePolicyResponse(
        UUID id,
        String code,
        String name,
        FeePolicyType policyType,
        String countryCode,
        String currency,
        String emitterCode,
        String beneficiaryCode,
        String railCode,
        UUID feeRuleId,
        String feeRuleCode,
        boolean actif,
        Integer ordre
) {}
