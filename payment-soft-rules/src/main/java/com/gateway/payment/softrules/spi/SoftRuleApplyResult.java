package com.gateway.payment.softrules.spi;

import java.util.List;
import java.util.Map;

/** Résultat d'un {@link SoftRuleHandler} — events audit + vars Flowable. */
public record SoftRuleApplyResult(
        List<Event> events,
        Map<String, Object> executionVars
) {

    public record Event(String type, String message) {}

    public static SoftRuleApplyResult of(String type, String message) {
        return new SoftRuleApplyResult(List.of(new Event(type, message)), Map.of());
    }

    public static SoftRuleApplyResult of(String type, String message, Map<String, Object> vars) {
        return new SoftRuleApplyResult(List.of(new Event(type, message)), Map.copyOf(vars));
    }

    public static SoftRuleApplyResult empty() {
        return new SoftRuleApplyResult(List.of(), Map.of());
    }
}
