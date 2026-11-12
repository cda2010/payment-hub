package com.gateway.payment.commission.moteur;

import java.math.BigDecimal;
import java.util.Map;

public record DemandeSimulation(
        String transactionId,
        String operation,
        String productCode,
        String channel,
        String currency,
        BigDecimal amount,
        BigDecimal fees,
        Map<String, BigDecimal> aggregates
) {}
