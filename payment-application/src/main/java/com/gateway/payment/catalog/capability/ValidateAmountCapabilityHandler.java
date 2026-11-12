package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValidateAmountCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public ValidateAmountCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.VALIDATE_AMOUNT;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }
}
