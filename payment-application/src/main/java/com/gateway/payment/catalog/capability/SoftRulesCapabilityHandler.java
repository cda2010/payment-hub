package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.Map;

/** Exécute uniquement les {@code softRules[]} de l'étape (capability générique). */
@Component
public class SoftRulesCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public SoftRulesCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.SOFT_RULES;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }
}
