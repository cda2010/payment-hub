package com.gateway.payment.catalog.capability;

import com.gateway.payment.inbox.PartyActionProducer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

@Component
public class GenerateCodeCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;
    private final ObjectProvider<PartyActionProducer> partyActions;

    public GenerateCodeCapabilityHandler(
            SoftRuleCapabilitySupport softRules,
            ObjectProvider<PartyActionProducer> partyActions
    ) {
        this.softRules = softRules;
        this.partyActions = partyActions;
    }

    @Override
    public String code() {
        return CapabilityCodes.GENERATE_CODE;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        PartyActionProducer producer = partyActions.getIfAvailable();
        if (producer != null) {
            UUID partyId = PartyActionProducer.partyIdFromContext(context.context());
            String paymentId = context.paymentId();
            String hint = firstCode(context.context(), soft.vars());
            if (partyId != null) {
                String tenant = context.transaction() != null ? context.transaction().getTenantId() : null;
                producer.cashoutCodeActive(partyId, tenant, paymentId,
                        StringUtils.hasText(hint) ? "Code : " + hint : "À utiliser avant expiration");
            }
        }
        return CapabilityResult.softOnly(soft.matched(), soft.events(), soft.vars());
    }

    private static String firstCode(Map<String, Object> ctx, Map<String, Object> vars) {
        for (String key : new String[]{"voucherCode", "withdrawalCode", "generatedCode", "code"}) {
            Object v = vars != null ? vars.get(key) : null;
            if (v == null && ctx != null) {
                v = ctx.get(key);
            }
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }
}
