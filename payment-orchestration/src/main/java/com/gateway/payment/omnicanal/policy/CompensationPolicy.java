package com.gateway.payment.omnicanal.policy;

public enum CompensationPolicy {
    NONE,
    AUTO_REVERSE,
    MANUAL;

    public static CompensationPolicy fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        try {
            return CompensationPolicy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    public boolean requiresReverse() {
        return this == AUTO_REVERSE;
    }
}
