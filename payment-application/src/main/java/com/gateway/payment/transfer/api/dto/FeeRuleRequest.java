package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.FeeBearer;
import com.gateway.payment.transfer.domain.FeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record FeeRuleRequest(
        @NotBlank String code,
        @NotBlank String name,
        String currency,
        String description,
        @NotNull Boolean actif,
        FeeBearer feeBearer,
        List<FeeRuleLineRequest> lines
) {
    public record FeeRuleLineRequest(
            @NotNull FeeType feeType,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            @NotNull BigDecimal value,
            BigDecimal fixedAmount,
            BigDecimal percentValue,
            BigDecimal minFee,
            BigDecimal maxFee,
            Integer ordre
    ) {}
}
