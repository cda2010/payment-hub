package com.gateway.payment.softrules;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import com.gateway.payment.softrules.spi.SoftRuleRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exécute les {@code softRules[]} déclarées sur la config catalogue pour une activité BPMN.
 * Dispatch par {@link SoftRuleRegistry} — nouveau type = nouveau {@link SoftRuleHandler}.
 */
@Component
public class CatalogSoftRuleEngine {

    private final SoftRuleRegistry registry;

    public CatalogSoftRuleEngine(SoftRuleRegistry registry) {
        this.registry = registry;
    }

    /** Hors Spring (tests) — builtins du module. */
    public static CatalogSoftRuleEngine withBuiltins() {
        return new CatalogSoftRuleEngine(com.gateway.payment.softrules.builtin.BuiltinSoftRules.registry());
    }

    public record AppliedEvent(String type, String message) {}

    public record SoftRuleOutcome(boolean matched, List<AppliedEvent> events, Map<String, Object> executionVars) {}

    public SoftRuleOutcome apply(String step, Map<String, Object> context) {
        List<Map<String, Object>> rules = rulesForStep(context, step);
        if (rules.isEmpty()) {
            return new SoftRuleOutcome(false, List.of(), Map.of());
        }
        List<AppliedEvent> events = new ArrayList<>();
        Map<String, Object> vars = new LinkedHashMap<>();
        for (Map<String, Object> rule : rules) {
            String type = SoftRuleSupport.str(rule.get("type"));
            if (type == null) {
                continue;
            }
            SoftRuleHandler handler = registry.get(type).orElseThrow(() ->
                    new BusinessException("SOFT_RULE_UNKNOWN",
                            "Type de softRule inconnu: " + type + " (step=" + step + ")"));
            @SuppressWarnings("unchecked")
            Map<String, Object> params = rule.get("params") instanceof Map<?, ?> m
                    ? new LinkedHashMap<>((Map<String, Object>) m)
                    : Map.of();
            SoftRuleApplyResult result = handler.apply(new SoftRuleApplyContext(step, type, params, context));
            for (SoftRuleApplyResult.Event ev : result.events()) {
                events.add(new AppliedEvent(ev.type(), ev.message()));
            }
            if (result.executionVars() != null) {
                vars.putAll(result.executionVars());
            }
        }
        return new SoftRuleOutcome(true, events, vars);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rulesForStep(Map<String, Object> context, String step) {
        Object cfg = context.get("catalogConfiguration");
        if (!(cfg instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object soft = map.get("softRules");
        if (soft == null) {
            soft = map.get("soft_rules");
        }
        if (!(soft instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> matched = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> rule = (Map<String, Object>) raw;
            String ruleStep = SoftRuleSupport.str(rule.get("step"));
            if (ruleStep != null && ruleStep.equalsIgnoreCase(step)) {
                matched.add(rule);
            }
        }
        return matched;
    }
}
