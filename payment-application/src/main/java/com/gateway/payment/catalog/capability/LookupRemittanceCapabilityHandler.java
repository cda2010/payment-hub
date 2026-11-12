package com.gateway.payment.catalog.capability;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.remittance.service.RemittancePayoutSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capability {@code LOOKUP_REMITTANCE} — charge un envoi AVAILABLE_FOR_PAYOUT par référence.
 */
@Component
public class LookupRemittanceCapabilityHandler implements CapabilityHandler {

    private final RemittancePayoutSupport payoutSupport;

    public LookupRemittanceCapabilityHandler(RemittancePayoutSupport payoutSupport) {
        this.payoutSupport = payoutSupport;
    }

    @Override
    public String code() {
        return CapabilityCodes.LOOKUP_REMITTANCE;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        Map<String, Object> ctx = context.context();
        String ref = firstNonBlank(
                str(ctx.get("remittanceReference")),
                str(ctx.get("mtcn")),
                str(ctx.get("reference")),
                str(ctx.get("quoteId")),
                str(ctx.get("parentPaymentId")));
        RemittancePayoutSupport.RemittanceSendSnapshot snap = payoutSupport.lookupAvailable(ref);
        Map<String, Object> projected = payoutSupport.projectToReceiveContext(snap);
        ctx.putAll(projected);

        Map<String, Object> vars = new LinkedHashMap<>(projected);
        // Variable BPMN pour exclusive gateway
        vars.put("payoutMethod", snap.payoutMethod());
        context.setVar("payoutMethod", snap.payoutMethod());

        // Rôle PROVIDER pour EXECUTE_PAYOUT
        @SuppressWarnings("unchecked")
        Map<String, Object> roles = ctx.get("roleAssignments") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>();
        Object provider = projected.get("providerCode");
        if (provider != null) {
            roles.put("PROVIDER", String.valueOf(provider));
            ctx.put("providerCode", provider);
            vars.put("providerCode", provider);
        }
        ctx.put("roleAssignments", roles);
        vars.put("roleAssignments", roles);

        projected.forEach(context::setVar);
        context.setVar("roleAssignments", roles);

        PaymentTransaction tx = context.transaction();
        if (tx != null) {
            tx.setCurrentStep("LOOKUP_REMITTANCE");
            tx.applyStatus(PaymentStatus.PROCESSING);
            if (projected.get("receiveAmount") instanceof Number n) {
                tx.setAmount(new java.math.BigDecimal(String.valueOf(n)));
            }
            if (projected.get("receiveCurrency") != null) {
                tx.setCurrency(String.valueOf(projected.get("receiveCurrency")));
            }
            if (projected.get("providerCode") != null) {
                // keep host provider for BPMN; store remittance op in context
                ctx.putIfAbsent("remittanceProviderCode", projected.get("providerCode"));
            }
        }

        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        events.add(new CapabilityResult.CapabilityEvent(
                "REMITTANCE_LOOKED_UP",
                "Envoi " + snap.remittanceReference()
                        + " — " + snap.payoutMethod()
                        + " — parent=" + snap.sendTx().getPaymentId()));

        return CapabilityResult.provider(events, vars, CapabilityResult.TerminalEffect.PROCESSING, null);
    }

    private static String str(Object v) {
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
