package com.gateway.payment.omnicanal.concurrency;

public enum StepClaimResult {
    CLAIMED,
    ALREADY_DONE,
    BUSY,
    REJECTED
}
