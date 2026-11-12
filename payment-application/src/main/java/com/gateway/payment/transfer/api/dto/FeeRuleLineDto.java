package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.FeeType;

import java.math.BigDecimal;
import java.util.UUID;

public record FeeRuleLineDto(
        UUID id,
        FeeType feeType,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal value,
        BigDecimal fixedAmount,
        BigDecimal percentValue,
        BigDecimal minFee,
        BigDecimal maxFee,
        Integer ordre
) {}
