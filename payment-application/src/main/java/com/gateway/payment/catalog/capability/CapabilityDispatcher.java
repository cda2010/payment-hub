package com.gateway.payment.catalog.capability;

import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.events.DomainEventPublisher;
import com.gateway.payment.events.DomainEventTypes;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.settlement.PaymentSettlementService;
import com.gateway.payment.spend.SpendRecorder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Dispatch capability si un handler est résolu ; sinon {@link CapabilityResult#unhandled()}
 * pour laisser le chemin legacy intact.
 */
@Component
public class CapabilityDispatcher {

    private final CapabilityRegistry registry;
    private final CapabilityResolver resolver;
    private final DomainEventPublisher domainEvents;
    private final PaymentSettlementService settlement;
    private final ObjectProvider<SpendRecorder> spendRecorder;

    public CapabilityDispatcher(
            CapabilityRegistry registry,
            CapabilityResolver resolver,
            DomainEventPublisher domainEvents,
            PaymentSettlementService settlement,
            ObjectProvider<SpendRecorder> spendRecorder
    ) {
        this.registry = registry;
        this.resolver = resolver;
        this.domainEvents = domainEvents;
        this.settlement = settlement;
        this.spendRecorder = spendRecorder;
    }

    public CapabilityResult tryExecute(
            String paymentId,
            String step,
            PaymentTransaction tx,
            Map<String, Object> context,
            CatalogBindingDefinition binding,
            Map<String, Object> catalogConfiguration
    ) {
        return tryExecute(paymentId, step, tx, context, binding, catalogConfiguration, Map.of(), null, null);
    }

    public CapabilityResult tryExecute(
            String paymentId,
            String step,
            PaymentTransaction tx,
            Map<String, Object> context,
            CatalogBindingDefinition binding,
            Map<String, Object> catalogConfiguration,
            Map<String, Object> input,
            String operationHint,
            BiConsumer<String, Object> setExecutionVariable
    ) {
        Optional<String> code = operationHint != null
                ? resolver.resolveProvider(step, binding, operationHint)
                : resolver.resolve(step, binding);
        if (code.isEmpty()) {
            return CapabilityResult.unhandled();
        }
        Optional<CapabilityHandler> handler = registry.get(code.get());
        if (handler.isEmpty()) {
            return CapabilityResult.unhandled();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        if (binding != null && binding.params() != null) {
            params.putAll(binding.params());
        }
        context.put("catalogCapability", code.get());

        Map<String, Object> configuration = catalogConfiguration != null ? catalogConfiguration : Map.of();
        Map<String, Object> inputMap = input != null ? input : Map.of();
        CapabilityExecutionContext exec = new CapabilityExecutionContext(
                paymentId, step, tx, context, params, inputMap, binding, setExecutionVariable);
        return handler.get().execute(exec, configuration);
    }

    public void applyTerminal(PaymentTransaction tx, Map<String, Object> context, CapabilityResult result) {
        if (result == null || !result.handled()) {
            return;
        }
        switch (result.terminal()) {
            case SUCCESS -> {
                if (tx.applyStatus(PaymentStatus.SUCCESS)) {
                    tx.setCompletedAt(Instant.now());
                    tx.setCurrentStep("SUCCESS");
                    domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_SUCCEEDED);
                    settlement.onSuccess(tx);
                    SpendRecorder recorder = spendRecorder.getIfAvailable();
                    if (recorder != null) {
                        recorder.record(tx, context);
                    }
                }
            }
            case RETURNED_FOR_CORRECTION -> {
                tx.applyStatus(PaymentStatus.RETURNED_FOR_CORRECTION);
                tx.setCompletedAt(null);
                tx.setErrorCode("APPROVAL_RETURNED");
                if (result.statusMessage() != null) {
                    tx.setErrorMessage(result.statusMessage());
                }
            }
            case CANCELLED -> {
                if (tx.applyStatus(PaymentStatus.CANCELLED)) {
                    tx.setCompletedAt(Instant.now());
                    tx.setCurrentStep("FAILURE");
                    tx.setErrorCode("APPROVAL_REJECTED");
                    if (result.statusMessage() != null) {
                        tx.setErrorMessage(result.statusMessage());
                    }
                    domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_CANCELLED);
                    settlement.onFailure(tx);
                }
            }
            case OTP_REQUIRED -> tx.applyStatus(PaymentStatus.OTP_REQUIRED);
            case REDIRECT_REQUIRED -> tx.applyStatus(PaymentStatus.REDIRECT_REQUIRED);
            case WAITING_CALLBACK -> tx.applyStatus(PaymentStatus.WAITING_CALLBACK);
            case PROCESSING -> tx.applyStatus(PaymentStatus.PROCESSING);
            case NONE -> {
                // no-op (statut déjà posé par le handler si besoin)
            }
        }
    }

    public void applyVars(CapabilityResult result, BiConsumer<String, Object> setVar) {
        if (result == null || !result.handled()) {
            return;
        }
        result.executionVars().forEach(setVar);
    }

    public Optional<CapabilityHandler> findHandler(String code) {
        return registry.get(code);
    }
}
