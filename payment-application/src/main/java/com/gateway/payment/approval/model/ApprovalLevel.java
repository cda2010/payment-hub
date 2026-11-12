package com.gateway.payment.approval.model;

import java.util.List;

public record ApprovalLevel(
        int level,
        int requiredApprovals,
        List<ApprovalAssignment> assignments
) {
    public ApprovalLevel {
        if (requiredApprovals < 1) {
            requiredApprovals = 1;
        }
        if (assignments == null) {
            assignments = List.of();
        }
    }
}
