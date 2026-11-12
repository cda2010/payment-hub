package com.gateway.payment.omnicanal.policy;

public enum TimeoutPolicy {
    SHORT_3S(3000),
    STANDARD_5S(5000),
    FINANCIAL_15S(15000),
    LONG_30S(30000);

    private final long millis;

    TimeoutPolicy(long millis) {
        this.millis = millis;
    }

    public long millis() {
        return millis;
    }

    public static TimeoutPolicy fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return STANDARD_5S;
        }
        try {
            return TimeoutPolicy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STANDARD_5S;
        }
    }
}
