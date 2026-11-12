package com.gateway.payment.omnicanal.policy;

public enum ErrorType {
    TRANSIENT,
    TIMEOUT,
    BUSINESS_REJECTED,
    TECHNICAL,
    UNKNOWN;

    public ErrorClassification toLegacyClassification() {
        return switch (this) {
            case TRANSIENT, TIMEOUT -> ErrorClassification.TRANSIENT;
            case BUSINESS_REJECTED -> ErrorClassification.BUSINESS;
            default -> ErrorClassification.TECHNICAL;
        };
    }
}
