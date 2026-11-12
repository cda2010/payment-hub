package com.gateway.payment.omnicanal.policy;

import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;

/** Agrégat des politiques d'une ligne de schéma. */
public record StepPolicies(
        ExecutionMode executionMode,
        TimeoutPolicy timeoutPolicy,
        RetryPolicy retryPolicy,
        CompensationPolicy compensationPolicy,
        CompensationMode compensationMode,
        String compensationStepCode,
        DependencyPolicy dependency,
        StepCriticality criticality,
        boolean blocking,
        String errorPolicyCode
) {
    public static StepPolicies fromLigne(LigneSchemaEcriture line) {
        if (line == null) {
            return defaults();
        }
        return new StepPolicies(
                ExecutionMode.fromCode(line.getExecutionMode()),
                TimeoutPolicy.fromCode(line.getTimeoutPolicy()),
                line.getRetryMaxAttempts() != null
                        ? new RetryPolicy(
                        Math.max(1, line.getRetryMaxAttempts()),
                        line.getRetryInitialDelayMs() != null ? line.getRetryInitialDelayMs() : 200L,
                        line.getRetryMultiplier() != null ? line.getRetryMultiplier() : 1.0,
                        line.getRetryMaxDelayMs() != null ? line.getRetryMaxDelayMs() : 2000L
                )
                        : RetryPolicy.fromCode(line.getRetryPolicy()),
                CompensationPolicy.fromCode(line.getCompensationPolicy()),
                CompensationMode.fromCode(line.getCompensationMode()),
                line.getCompensationStepCode(),
                new DependencyPolicy(
                        line.getDependsOnStepCode(),
                        DependencyCondition.fromCode(line.getDependencyCondition()),
                        Boolean.TRUE.equals(line.getDependencyRequired())
                ),
                StepCriticality.fromCode(line.getCriticality()),
                line.getBlocking() == null || Boolean.TRUE.equals(line.getBlocking()),
                line.getErrorPolicyCode()
        );
    }

    public static StepPolicies defaults() {
        return new StepPolicies(
                ExecutionMode.SYNC,
                TimeoutPolicy.STANDARD_5S,
                RetryPolicy.none(),
                CompensationPolicy.NONE,
                CompensationMode.NONE,
                null,
                DependencyPolicy.none(),
                StepCriticality.REQUIRED,
                true,
                null
        );
    }
}
