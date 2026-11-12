package com.gateway.payment.omnicanal.concurrency;

import com.gateway.payment.omnicanal.policy.StepOutcome;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Machine d'état simple par step (miroir Omnicanal StepStateMachine).
 */
public class StepStateMachine {

    private final Map<String, StepOutcome> states = new ConcurrentHashMap<>();

    private static final Map<StepOutcome, Set<StepOutcome>> ALLOWED = Map.of(
            StepOutcome.PENDING, EnumSet.of(StepOutcome.SUCCESS, StepOutcome.FAILURE, StepOutcome.SKIPPED),
            StepOutcome.FAILURE, EnumSet.of(StepOutcome.COMPENSATED, StepOutcome.SUCCESS),
            StepOutcome.SUCCESS, EnumSet.of(StepOutcome.COMPENSATED)
    );

    public StepOutcome get(String stepKey) {
        return states.getOrDefault(stepKey, StepOutcome.PENDING);
    }

    public void transition(String stepKey, StepOutcome next) {
        StepOutcome current = get(stepKey);
        Set<StepOutcome> allowed = ALLOWED.getOrDefault(current, EnumSet.noneOf(StepOutcome.class));
        if (current == next) {
            return;
        }
        if (!allowed.contains(next) && current != StepOutcome.PENDING) {
            throw new IllegalStateException("Transition interdite " + current + " → " + next + " pour " + stepKey);
        }
        states.put(stepKey, next);
    }

    public boolean isDone(String stepKey) {
        StepOutcome o = get(stepKey);
        return o == StepOutcome.SUCCESS || o == StepOutcome.SKIPPED || o == StepOutcome.COMPENSATED;
    }
}
