package com.gateway.payment.rules.moteur;

import com.gateway.payment.rules.domain.RuleDecision;

import java.util.List;
import java.util.UUID;

public record ResultatEvaluation(
        String transactionId,
        String groupCode,
        RuleDecision decision,
        boolean authorized,
        boolean simulate,
        UUID reservationId,
        List<ViolationDetail> violations,
        List<String> evaluatedRules
) {
}
