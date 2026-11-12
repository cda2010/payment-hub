package com.gateway.payment.approval.model;

import com.gateway.payment.approval.domain.ApprovalAssignmentType;

import java.util.List;

public record ApprovalAssignment(
        ApprovalAssignmentType type,
        String value
) {
    public static ApprovalAssignment role(String role) {
        return new ApprovalAssignment(ApprovalAssignmentType.ROLE, role);
    }
}
