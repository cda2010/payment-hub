package com.gateway.payment.rules.moteur;

import com.gateway.payment.rules.domain.RuleDecision;

import java.math.BigDecimal;

public record ViolationDetail(
        String ruleCode,
        int ruleVersion,
        RuleDecision action,
        String message,
        BigDecimal currentValue,
        BigDecimal requestedValue,
        BigDecimal projectedValue,
        BigDecimal limitValue
) {
}
