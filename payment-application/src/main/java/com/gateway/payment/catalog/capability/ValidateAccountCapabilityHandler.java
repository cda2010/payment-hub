package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValidateAccountCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public ValidateAccountCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.VALIDATE_ACCOUNT;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }
}
