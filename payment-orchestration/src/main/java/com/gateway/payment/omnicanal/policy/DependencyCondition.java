package com.gateway.payment.omnicanal.policy;

public enum DependencyCondition {
    ON_SUCCESS,
    ON_FAILURE,
    ALWAYS;

    public static DependencyCondition fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return ON_SUCCESS;
        }
        try {
            return DependencyCondition.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ON_SUCCESS;
        }
    }
}
