package com.gateway.payment.catalog.capability;

import com.gateway.payment.remittance.service.RemittancePayoutSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capability {@code COMPLETE_PAYOUT} — clôture locale receive + parent send (après EXECUTE_PAYOUT).
 */
@Component
public class CompletePayoutCapabilityHandler implements CapabilityHandler {

    private final RemittancePayoutSupport payoutSupport;
    private final SoftRuleCapabilitySupport softRules;

    public CompletePayoutCapabilityHandler(
            RemittancePayoutSupport payoutSupport,
            SoftRuleCapabilitySupport softRules
    ) {
        this.payoutSupport = payoutSupport;
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.COMPLETE_PAYOUT;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        Map<String, Object> ctx = context.context();
        String parentId = ctx.get("parentPaymentId") != null
                ? String.valueOf(ctx.get("parentPaymentId"))
                : null;
        String receiveId = context.paymentId();
        String method = ctx.get("payoutMethod") != null
                ? String.valueOf(ctx.get("payoutMethod"))
                : "CASH_PICKUP";

        payoutSupport.markSendPaidOut(parentId, receiveId, method);

        String terminal = "CASH_PICKUP".equalsIgnoreCase(method) ? "PAID_OUT" : "CREDITED";
        ctx.put("remittanceStatus", terminal);
        ctx.put("availableForPayout", false);
        Map<String, Object> vars = new LinkedHashMap<>(soft.vars());
        vars.put("remittanceStatus", terminal);
        vars.put("availableForPayout", false);

        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        events.add(new CapabilityResult.CapabilityEvent(
                terminal, "Payout terminé — parent " + parentId + " clôturé"));

        return CapabilityResult.of(
                soft.matched(),
                events,
                vars,
                CapabilityResult.TerminalEffect.SUCCESS,
                "Remittance " + terminal);
    }
}
