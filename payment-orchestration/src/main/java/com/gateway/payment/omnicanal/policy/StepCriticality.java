package com.gateway.payment.omnicanal.policy;

public enum StepCriticality {
    CRITICAL,
    REQUIRED,
    OPTIONAL,
    INFORMATIONAL;

    public static StepCriticality fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return REQUIRED;
        }
        try {
            return StepCriticality.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return REQUIRED;
        }
    }

    public boolean allowsContinue() {
        return this == OPTIONAL || this == INFORMATIONAL;
    }

    public boolean prefersStopOrCompensate() {
        return this == CRITICAL || this == REQUIRED;
    }
}
