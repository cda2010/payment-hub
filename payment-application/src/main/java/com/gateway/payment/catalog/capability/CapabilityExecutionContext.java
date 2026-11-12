package com.gateway.payment.catalog.capability;

import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.payment.domain.PaymentTransaction;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Contexte d'exécution d'une capability (TX + étape BPMN + context JSON mutable).
 */
public record CapabilityExecutionContext(
        String paymentId,
        String step,
        PaymentTransaction transaction,
        Map<String, Object> context,
        Map<String, Object> bindingParams,
        Map<String, Object> input,
        CatalogBindingDefinition binding,
        BiConsumer<String, Object> setExecutionVariable
) {
    public static CapabilityExecutionContext local(
            String paymentId,
            String step,
            PaymentTransaction transaction,
            Map<String, Object> context,
            Map<String, Object> bindingParams
    ) {
        return new CapabilityExecutionContext(
                paymentId, step, transaction, context, bindingParams, Map.of(), null, null);
    }

    public void setVar(String key, Object value) {
        if (setExecutionVariable != null) {
            setExecutionVariable.accept(key, value);
        }
    }
}
