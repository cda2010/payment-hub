package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ApprovalOutcomeCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public ApprovalOutcomeCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.APPROVAL_OUTCOME;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        String step = context.step() != null ? context.step().toUpperCase(Locale.ROOT) : "";

        if ("APPROVAL_REJECTED".equals(step)) {
            Object comment = context.context().get("approvalComment");
            String msg = comment != null && !String.valueOf(comment).isBlank()
                    ? "Approbation refusée: " + comment
                    : "Approbation refusée";
            context.context().put("waitMessage", msg);
            events.add(new CapabilityResult.CapabilityEvent("APPROVAL_REJECTED", msg));
            Map<String, Object> vars = soft.vars();
            vars.put("waitMessage", msg);
            return CapabilityResult.of(
                    soft.matched(), events, vars, CapabilityResult.TerminalEffect.CANCELLED, msg);
        }

        Object comment = context.context().get("approvalComment");
        String msg = comment != null && !String.valueOf(comment).isBlank()
                ? "Approbation refusée — à corriger: " + comment
                : "Approbation refusée — corrigez puis reconfirmez";
        context.context().put("waitMessage", msg);
        events.add(new CapabilityResult.CapabilityEvent("APPROVAL_RETURNED", msg));
        Map<String, Object> vars = soft.vars();
        vars.put("waitMessage", msg);
        return CapabilityResult.of(
                soft.matched(), events, vars, CapabilityResult.TerminalEffect.RETURNED_FOR_CORRECTION, msg);
    }
}
