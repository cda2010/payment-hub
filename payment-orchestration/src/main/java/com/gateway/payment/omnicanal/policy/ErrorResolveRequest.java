package com.gateway.payment.omnicanal.policy;

import java.util.UUID;

public record ErrorResolveRequest(
        String stepKey,
        String orchErrorCode,
        Integer causeErrCode,
        String message,
        StepCriticality criticality,
        String stepErrorPolicyCode,
        String schemaErrorPolicyCode,
        UUID schemaEcritureId,
        ErrorAction stepDefaultAction,
        WorkflowErrorPolicy workflowPolicy
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String stepKey;
        private String orchErrorCode;
        private Integer causeErrCode;
        private String message;
        private StepCriticality criticality;
        private String stepErrorPolicyCode;
        private String schemaErrorPolicyCode;
        private UUID schemaEcritureId;
        private ErrorAction stepDefaultAction;
        private WorkflowErrorPolicy workflowPolicy;

        public Builder stepKey(String v) { this.stepKey = v; return this; }
        public Builder orchErrorCode(String v) { this.orchErrorCode = v; return this; }
        public Builder causeErrCode(Integer v) { this.causeErrCode = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder criticality(StepCriticality v) { this.criticality = v; return this; }
        public Builder stepErrorPolicyCode(String v) { this.stepErrorPolicyCode = v; return this; }
        public Builder schemaErrorPolicyCode(String v) { this.schemaErrorPolicyCode = v; return this; }
        public Builder schemaEcritureId(UUID v) { this.schemaEcritureId = v; return this; }
        public Builder stepDefaultAction(ErrorAction v) { this.stepDefaultAction = v; return this; }
        public Builder workflowPolicy(WorkflowErrorPolicy v) { this.workflowPolicy = v; return this; }

        public ErrorResolveRequest build() {
            return new ErrorResolveRequest(
                    stepKey, orchErrorCode, causeErrCode, message, criticality,
                    stepErrorPolicyCode, schemaErrorPolicyCode, schemaEcritureId,
                    stepDefaultAction, workflowPolicy
            );
        }
    }
}
