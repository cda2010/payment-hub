package com.gateway.payment.commission.moteur;

import java.math.BigDecimal;
import java.util.UUID;

public record PartCommission(
        UUID ruleId,
        String ruleCode,
        String beneficiaryRole,
        String formuleCode,
        String commissionBase,
        BigDecimal baseAmount,
        BigDecimal commissionAmount,
        String fundingType,
        String funderRole,
        String detail
) {}
