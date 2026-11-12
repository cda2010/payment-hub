package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Attente settlement / agent / payout — messages dérivés du step + params.
 * Ne fabrique plus d'état métier (ex. {@code AVAILABLE_FOR_PAYOUT}) : le provider / callback le pose.
 */
@Component
public class WaitCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public WaitCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.WAIT;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        if (!soft.matched()) {
            events.add(new CapabilityResult.CapabilityEvent(
                    context.step(), "En attente de confirmation / settlement"));
        }

        String step = context.step() != null ? context.step().toUpperCase(Locale.ROOT) : "";
        String waitMessage = resolveWaitMessage(step, context.context(), context.bindingParams());
        context.context().put("waitMessage", waitMessage);
        Map<String, Object> vars = soft.vars();
        vars.put("waitMessage", waitMessage);

        if ("WAIT_PAYOUT".equals(step) || "WAIT_REMITTANCE_PAYOUT".equals(step)) {
            // Conserve une référence déjà fournie par le provider — ne génère / n'invente pas le statut
            Object ref = firstNonBlank(
                    str(context.context().get("remittanceReference")),
                    str(context.context().get("providerReference")),
                    context.transaction() != null ? context.transaction().getProviderReference() : null);
            if (ref != null) {
                context.context().putIfAbsent("remittanceReference", ref);
                vars.putIfAbsent("remittanceReference", ref);
            }
            events.add(new CapabilityResult.CapabilityEvent("WAIT_PAYOUT", waitMessage));
            return CapabilityResult.of(
                    soft.matched(), events, vars, CapabilityResult.TerminalEffect.WAITING_CALLBACK, waitMessage);
        }

        return CapabilityResult.softOnly(soft.matched(), events, vars);
    }

    private static String resolveWaitMessage(String step, Map<String, Object> context, Map<String, Object> params) {
        if (params != null) {
            Object custom = params.get("message");
            if (custom == null) {
                custom = params.get("waitMessage");
            }
            if (custom != null && StringUtils.hasText(String.valueOf(custom))) {
                return String.valueOf(custom);
            }
        }
        if ("WAIT_AGENT_PAYOUT".equals(step)) {
            return "En attente de remise cash par l'agent";
        }
        if ("WAIT_GAB_WITHDRAWAL".equals(step)) {
            Object code = context.get("cashOutCode");
            return code != null
                    ? "Code retrait " + code + " — saisissez-le au GAB"
                    : "En attente de retrait au GAB";
        }
        if ("WAIT_AGENT_COLLECT".equals(step)) {
            Object code = context.get("cashInCode");
            return code != null
                    ? "Code dépôt " + code + " — remettez le cash à l'agent"
                    : "En attente d'encaissement cash par l'agent";
        }
        if ("WAIT_PAYOUT".equals(step) || "WAIT_REMITTANCE_PAYOUT".equals(step)) {
            Object ref = context.get("remittanceReference");
            if (ref == null) {
                ref = context.get("providerReference");
            }
            return ref != null
                    ? "Envoi " + ref + " — en attente de disponibilité payout (callback provider)"
                    : "En attente de disponibilité payout (callback provider)";
        }
        Object existing = context.get("waitMessage");
        if (existing != null && StringUtils.hasText(String.valueOf(existing))) {
            return String.valueOf(existing);
        }
        return "En attente de règlement";
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
