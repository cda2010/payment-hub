package com.gateway.payment.omnicanal.response;

public class OrchestrationException extends RuntimeException {
    private final OrchErrorCode errorCode;
    private final String stepKey;

    public OrchestrationException(OrchErrorCode errorCode, String message, String stepKey) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : OrchErrorCode.ORCH_INTERNAL;
        this.stepKey = stepKey;
    }

    public OrchestrationException(OrchErrorCode errorCode, String message, String stepKey, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : OrchErrorCode.ORCH_INTERNAL;
        this.stepKey = stepKey;
    }

    public OrchErrorCode getErrorCode() { return errorCode; }
    public String getStepKey() { return stepKey; }
}
