package com.gateway.payment.omnicanal.policy;

public enum CompensationMode {
    NONE,
    AUTO_REVERSE,
    MANUAL,
    REVERSE_DESC;

    public static CompensationMode fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        try {
            return CompensationMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
