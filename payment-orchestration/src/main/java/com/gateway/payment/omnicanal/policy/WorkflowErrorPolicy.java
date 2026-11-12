package com.gateway.payment.omnicanal.policy;

public record WorkflowErrorPolicy(
        ErrorAction defaultAction,
        boolean stopOnBusinessError,
        boolean requireManualReviewOnUnknown,
        boolean compensateCompletedCriticalSteps,
        boolean allowPartialSuccess
) {
    public static WorkflowErrorPolicy globalDefault() {
        return new WorkflowErrorPolicy(
                ErrorAction.STOP_WORKFLOW,
                true,
                true,
                true,
                false
        );
    }

    public static WorkflowErrorPolicy interFse() {
        return globalDefault();
    }

    /**
     * Dérive la politique workflow depuis {@code schema.error_policy_code}
     * (flags booléens schéma Omnicanal non portés → défauts globaux).
     */
    public static WorkflowErrorPolicy fromSchema(com.gateway.payment.orchestration.domain.SchemaEcriture schema) {
        if (schema == null) {
            return globalDefault();
        }
        String code = schema.getErrorPolicyCode();
        if (code != null) {
            String n = code.trim().toUpperCase();
            if (n.contains("INTER") || n.contains("DIFF")) {
                return interFse();
            }
        }
        return globalDefault();
    }
}
