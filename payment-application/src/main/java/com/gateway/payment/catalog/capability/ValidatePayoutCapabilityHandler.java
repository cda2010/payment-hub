package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Capability {@code VALIDATE_PAYOUT} — contrôles locaux avant remise (ID, confirm…).
 */
@Component
public class ValidatePayoutCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public ValidatePayoutCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.VALIDATE_PAYOUT;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }
}
