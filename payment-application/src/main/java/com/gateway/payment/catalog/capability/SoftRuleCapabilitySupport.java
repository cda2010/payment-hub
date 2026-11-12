package com.gateway.payment.catalog.capability;

import com.gateway.payment.softrules.CatalogSoftRuleEngine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pont softRules → Capability (réutilise le moteur existant, zéro duplication métier).
 */
@Component
public class SoftRuleCapabilitySupport {

    private final CatalogSoftRuleEngine softRuleEngine;

    public SoftRuleCapabilitySupport(CatalogSoftRuleEngine softRuleEngine) {
        this.softRuleEngine = softRuleEngine;
    }

    public SoftBundle apply(String step, Map<String, Object> context) {
        CatalogSoftRuleEngine.SoftRuleOutcome soft = softRuleEngine.apply(step, context);
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        for (CatalogSoftRuleEngine.AppliedEvent ev : soft.events()) {
            events.add(new CapabilityResult.CapabilityEvent(ev.type(), ev.message()));
        }
        return new SoftBundle(soft.matched(), events, new LinkedHashMap<>(soft.executionVars()));
    }

    public record SoftBundle(
            boolean matched,
            List<CapabilityResult.CapabilityEvent> events,
            Map<String, Object> vars
    ) {}
}
