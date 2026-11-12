package com.gateway.payment.transfer.api.dto;

import com.gateway.payment.transfer.domain.FeeBearer;
import com.gateway.payment.transfer.domain.RailScope;

import java.math.BigDecimal;

public record TransferModeOptionResponse(
        String railCode,
        String railName,
        String displayLabel,
        String etaLabel,
        String warning,
        String description,
        RailScope railScope,
        boolean recommended,
        BigDecimal amount,
        BigDecimal feeAmount,
        BigDecimal totalDebit,
        BigDecimal amountReceived,
        FeeBearer feeBearer,
        String feeBreakdown,
        String currency
) {}
