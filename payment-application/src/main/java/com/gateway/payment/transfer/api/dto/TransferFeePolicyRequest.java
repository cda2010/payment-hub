package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.FeePolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferFeePolicyRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull FeePolicyType policyType,
        String countryCode,
        String currency,
        String emitterCode,
        String beneficiaryCode,
        String railCode,
        @NotNull UUID feeRuleId,
        @NotNull Boolean actif,
        Integer ordre
) {}
