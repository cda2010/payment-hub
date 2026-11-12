package com.gateway.payment.approval.model;

import java.util.List;

/**
 * Circuit de validation résolu (sortie logique après DMN + lookup profile).
 */
public record ApprovalProfile(
        String code,
        String strategy,
        List<ApprovalLevel> levels
) {
    public ApprovalProfile {
        if (strategy == null || strategy.isBlank()) {
            strategy = "SEQUENTIAL";
        }
        if (levels == null) {
            levels = List.of();
        }
    }

    public boolean requiresApproval() {
        return levels != null && !levels.isEmpty();
    }

    public int levelCount() {
        return levels == null ? 0 : levels.size();
    }

    public static ApprovalProfile none(String code) {
        return new ApprovalProfile(code != null ? code : "NO_APPROVAL", "SEQUENTIAL", List.of());
    }
}
