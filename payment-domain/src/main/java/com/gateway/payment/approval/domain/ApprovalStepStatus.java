package com.gateway.payment.approval.domain;

public enum ApprovalStepStatus {
    PENDING,
    WAITING,
    APPROVED,
    REJECTED,
    SKIPPED,
    INVALIDATED
}
