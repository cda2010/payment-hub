package com.gateway.payment.omnicanal.policy;

/** Action décidée par {@link ErrorPolicyResolver} (miroir Omnicanal). */
public enum ErrorAction {
    FAIL,
    RETRY,
    CHECK_STATUS,
    WAIT_CALLBACK,
    COMPENSATE,
    MANUAL_REVIEW,
    ESCALATE,
    CONTINUE,
    STOP_WORKFLOW,
    PROPAGATE;

    public static ErrorAction fromCode(String raw) {
        ErrorAction parsed = parseOptional(raw);
        return parsed != null ? parsed : STOP_WORKFLOW;
    }

    /** {@code null} si vide — laisse la politique workflow / table décider. */
    public static ErrorAction parseOptional(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase().replace('-', '_');
        if ("FAIL".equals(n)) {
            return STOP_WORKFLOW;
        }
        try {
            return ErrorAction.valueOf(n);
        } catch (IllegalArgumentException e) {
            return STOP_WORKFLOW;
        }
    }

    public ErrorAction normalized() {
        return this == FAIL ? STOP_WORKFLOW : this;
    }

    public boolean isPendingResolution() {
        ErrorAction n = normalized();
        return n == CHECK_STATUS || n == WAIT_CALLBACK || n == MANUAL_REVIEW || n == ESCALATE;
    }

    public boolean stopsWorkflow() {
        ErrorAction n = normalized();
        return n == STOP_WORKFLOW || n == FAIL || n == PROPAGATE;
    }

    public boolean triggersCompensation() {
        return normalized() == COMPENSATE;
    }
}
