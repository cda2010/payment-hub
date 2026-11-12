package com.gateway.payment.omnicanal.policy;

public enum ExecutionMode {
    SYNC,
    ASYNC_CALLBACK,
    ASYNC_POLL,
    FIRE_AND_FORGET;

    public static ExecutionMode fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return SYNC;
        }
        try {
            return ExecutionMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SYNC;
        }
    }
}
