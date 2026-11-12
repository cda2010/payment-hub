package com.gateway.payment.omnicanal.policy;

public record DependencyPolicy(
        String dependsOnStepCode,
        DependencyCondition condition,
        boolean required
) {
    public static DependencyPolicy none() {
        return new DependencyPolicy(null, DependencyCondition.ON_SUCCESS, false);
    }

    public boolean hasDependency() {
        return dependsOnStepCode != null && !dependsOnStepCode.isBlank();
    }
}
