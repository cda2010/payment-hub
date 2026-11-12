package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.bpmn.BpmnStepAttributes;
import com.gateway.payment.catalog.bpmn.CatalogStepBindings;
import com.gateway.payment.catalog.capability.CapabilityDispatcher;
import com.gateway.payment.catalog.capability.CapabilityResult;
import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogUiBindingResolver;
import com.gateway.payment.softrules.CatalogSoftRuleEngine;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentEvent;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Étapes produit catalogue (SOFT / WAIT / NOTIFY / approval return / capability).
 * Préfère {@link CapabilityDispatcher} quand un handler est résolu (inférence step ou
 * {@code bindings[].capability}) ; sinon conserve le chemin softRules + branches legacy.
 */
@Component("gatewayCatalogStepDelegate")
public class CatalogProductStepDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CatalogProductStepDelegate.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final CatalogUiBindingResolver uiBindingResolver;
    private final CatalogSoftRuleEngine softRuleEngine;
    private final CapabilityDispatcher capabilityDispatcher;

    public CatalogProductStepDelegate(
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper,
            CatalogUiBindingResolver uiBindingResolver,
            CatalogSoftRuleEngine softRuleEngine,
            CapabilityDispatcher capabilityDispatcher
    ) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.uiBindingResolver = uiBindingResolver;
        this.softRuleEngine = softRuleEngine;
        this.capabilityDispatcher = capabilityDispatcher;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String paymentId = stringVar(execution, PaymentProcessSupport.VAR_PAYMENT_ID);
        if (paymentId == null || paymentId.isBlank()) {
            throw new BusinessException("PAYMENT_ID_MISSING", "paymentId manquant dans le processus");
        }
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable: " + paymentId));

        String step = execution.getCurrentActivityId();
        if (step == null || step.isBlank()) {
            step = "CATALOG_STEP";
        }

        Map<String, Object> context = readContext(tx);
        execution.getVariables().forEach((k, v) -> {
            if (v != null && !"otp".equalsIgnoreCase(k)) {
                context.put(k, v);
            }
        });

        tx.setCurrentStep(step);
        tx.applyStatus(PaymentStatus.PROCESSING);
        context.put("lastCatalogStep", step);

        CatalogBindingDefinition binding = CatalogStepBindings.merge(
                uiBindingResolver.findInvokeBinding(context, step).orElse(null),
                BpmnStepAttributes.of(execution),
                step);
        if (binding != null) {
            context.put("catalogBindingKind", binding.kind().name());
            if (binding.actorRole() != null) {
                context.put("catalogActorRole", binding.actorRole());
                if (context.get("roleAssignments") instanceof Map<?, ?> roles) {
                    Object provider = roles.get(binding.actorRole());
                    if (provider != null) {
                        context.put("catalogActorProvider", String.valueOf(provider));
                    }
                }
            }
            if (binding.operation() != null) {
                context.put("catalogOperation", binding.operation());
            }
            if (binding.capability() != null) {
                context.put("catalogCapability", binding.capability());
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> catalogConfiguration = context.get("catalogConfiguration") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();

        CapabilityResult capability = capabilityDispatcher.tryExecute(
                paymentId, step, tx, context, binding, catalogConfiguration);

        if (capability.handled()) {
            capabilityDispatcher.applyVars(capability, execution::setVariable);
            for (CapabilityResult.CapabilityEvent ev : capability.events()) {
                addEvent(paymentId, ev.type(), ev.message());
            }
            capabilityDispatcher.applyTerminal(tx, context, capability);
            if (capability.terminal() == CapabilityResult.TerminalEffect.NONE
                    && !capability.softRulesMatched()
                    && !"CALCULATE_FEES".equals(step)
                    && !"calculateFees".equals(step)
                    && !step.startsWith("WAIT_")) {
                addEvent(paymentId, "CATALOG_STEP", "Étape produit " + step);
            }
            tx.setContextJson(writeJson(context));
            transactionRepository.save(tx);
            execution.setVariable("providerSuccess", true);
            log.info("Catalog capability ok paymentId={} step={} capability={} softRules={}",
                    paymentId, step, context.get("catalogCapability"), capability.softRulesMatched());
            return;
        }

        // ── Legacy (inchangé) si aucune capability résolue ─────────────────
        CatalogSoftRuleEngine.SoftRuleOutcome soft = softRuleEngine.apply(step, context);
        soft.executionVars().forEach(execution::setVariable);
        for (CatalogSoftRuleEngine.AppliedEvent ev : soft.events()) {
            addEvent(paymentId, ev.type(), ev.message());
        }

        if (("CALCULATE_FEES".equals(step) || "calculateFees".equals(step)) && context.get("fees") == null) {
            context.put("fees", 0);
            execution.setVariable("fees", 0);
        }
        if ("WAIT_SETTLEMENT".equals(step) || "WAIT_CONFIRMATION".equals(step)
                || "WAIT_AGENT_PAYOUT".equals(step) || "WAIT_AGENT_COLLECT".equals(step)) {
            if (!soft.matched()) {
                addEvent(paymentId, step, "En attente de confirmation / settlement");
            }
            if ("WAIT_AGENT_PAYOUT".equals(step)) {
                Object code = context.get("cashOutCode");
                context.put("waitMessage", code != null
                        ? "Code retrait " + code + " — présentez-vous chez l'agent"
                        : "En attente de remise cash par l'agent");
            } else if ("WAIT_AGENT_COLLECT".equals(step)) {
                Object code = context.get("cashInCode");
                context.put("waitMessage", code != null
                        ? "Code dépôt " + code + " — remettez le cash à l'agent"
                        : "En attente d'encaissement cash par l'agent");
            } else {
                context.putIfAbsent("waitMessage", "En attente de règlement");
            }
        }
        if ("NOTIFY".equals(step)) {
            tx.applyStatus(PaymentStatus.SUCCESS);
            tx.setCompletedAt(Instant.now());
            tx.setCurrentStep("SUCCESS");
            addEvent(paymentId, "NOTIFY", "Notification produit catalogue");
            addEvent(paymentId, "SUCCESS", "Transfert terminé (parcours catalogue)");
        } else if ("APPROVAL_RETURNED".equals(step) || "RETURN_TO_INITIATOR".equals(step)) {
            tx.applyStatus(PaymentStatus.RETURNED_FOR_CORRECTION);
            tx.setCompletedAt(null);
            tx.setCurrentStep(step);
            tx.setErrorCode("APPROVAL_RETURNED");
            Object comment = context.get("approvalComment");
            String msg = comment != null && !String.valueOf(comment).isBlank()
                    ? "Approbation refusée — à corriger: " + comment
                    : "Approbation refusée — corrigez puis reconfirmez";
            tx.setErrorMessage(msg);
            context.put("waitMessage", msg);
            addEvent(paymentId, "APPROVAL_RETURNED", msg);
        } else if ("APPROVAL_REJECTED".equals(step)) {
            tx.applyStatus(PaymentStatus.CANCELLED);
            tx.setCompletedAt(Instant.now());
            tx.setCurrentStep("FAILURE");
            tx.setErrorCode("APPROVAL_REJECTED");
            Object comment = context.get("approvalComment");
            tx.setErrorMessage(comment != null && !String.valueOf(comment).isBlank()
                    ? "Approbation refusée: " + comment
                    : "Approbation refusée");
            context.put("waitMessage", tx.getErrorMessage());
            addEvent(paymentId, "APPROVAL_REJECTED", tx.getErrorMessage());
        } else if (!soft.matched()
                && !"WAIT_SETTLEMENT".equals(step)
                && !"WAIT_CONFIRMATION".equals(step)
                && !"WAIT_AGENT_PAYOUT".equals(step)
                && !"WAIT_AGENT_COLLECT".equals(step)
                && !"CALCULATE_FEES".equals(step)
                && !"calculateFees".equals(step)) {
            addEvent(paymentId, "CATALOG_STEP", "Étape produit " + step);
        }

        tx.setContextJson(writeJson(context));
        transactionRepository.save(tx);
        execution.setVariable("providerSuccess", true);
        log.info("Catalog step ok paymentId={} step={} softRules={}", paymentId, step, soft.matched());
    }

    private static String stringVar(DelegateExecution execution, String name) {
        Object v = execution.getVariable(name);
        return v != null ? String.valueOf(v) : null;
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void addEvent(String paymentId, String type, String message) {
        eventRepository.save(PaymentEvent.builder()
                .paymentId(paymentId)
                .eventType(type)
                .message(message)
                .createdAt(Instant.now())
                .build());
    }
}
