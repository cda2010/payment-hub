package com.gateway.payment.omnicanal.policy;

public record RetryPolicy(
        int maxAttempts,
        long initialDelayMs,
        double multiplier,
        long maxDelayMs
) {
    public static RetryPolicy none() {
        return new RetryPolicy(1, 0, 1.0, 0);
    }

    public static RetryPolicy fromCode(String code) {
        if (code == null || code.isBlank() || "NONE".equalsIgnoreCase(code)) {
            return none();
        }
        return switch (code.trim().toUpperCase()) {
            case "TRANSIENT_3X" -> new RetryPolicy(3, 200, 1.0, 200);
            case "TRANSIENT_2X_BACKOFF" -> new RetryPolicy(2, 200, 2.0, 2000);
            default -> none();
        };
    }

    public boolean allowsRetry(int attempt) {
        return attempt < maxAttempts;
    }
}
