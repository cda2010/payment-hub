package com.gateway.payment.omnicanal.policy;

public record ErrorDecision(
        ErrorType errorType,
        ErrorAction action,
        Integer maxRetries,
        Long retryDelayMs,
        String targetStepCode,
        StepCriticality criticality,
        boolean propagateToParent,
        boolean compensate,
        String ruleSource,
        String ruleCode,
        String rationale
) {
    public static ErrorDecision of(
            ErrorType errorType,
            ErrorAction action,
            Integer maxRetries,
            Long retryDelayMs,
            String targetStepCode,
            StepCriticality criticality,
            boolean propagateToParent,
            boolean compensate,
            String ruleSource,
            String ruleCode,
            String rationale
    ) {
        return new ErrorDecision(
                errorType, action, maxRetries, retryDelayMs, targetStepCode,
                criticality, propagateToParent, compensate, ruleSource, ruleCode, rationale
        );
    }

    public ErrorAction actionNormalized() {
        return action != null ? action.normalized() : ErrorAction.STOP_WORKFLOW;
    }
}
