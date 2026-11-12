package com.gateway.payment.approval.model;

import java.util.List;

/**
 * Policy catalogue accrochée au ProductStep APPROVAL.
 */
public record ApprovalPolicy(
        String code,
        boolean enabled,
        String strategy,
        String dmnDecisionKey,
        boolean initiatorCanApprove,
        boolean sameUserAcrossLevelsAllowed,
        String formKey,
        List<String> criticalFields
) {
    public ApprovalPolicy {
        if (strategy == null || strategy.isBlank()) {
            strategy = "SEQUENTIAL";
        }
        if (formKey == null || formKey.isBlank()) {
            formKey = "account-transfer-approval";
        }
        if (criticalFields == null) {
            criticalFields = List.of("amount", "currency", "destAccount", "sourceAccount");
        }
    }

    public static ApprovalPolicy disabled() {
        return new ApprovalPolicy(
                null, false, "SEQUENTIAL", null, false, false,
                "account-transfer-approval", List.of());
    }
}
