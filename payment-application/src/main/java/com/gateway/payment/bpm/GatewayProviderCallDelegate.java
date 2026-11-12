package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.bpmn.BpmnStepAttributes;
import com.gateway.payment.catalog.bpmn.CatalogStepBindings;
import com.gateway.payment.catalog.capability.CapabilityDispatcher;
import com.gateway.payment.catalog.capability.CapabilityExecutionContext;
import com.gateway.payment.catalog.capability.CapabilityResult;
import com.gateway.payment.catalog.capability.ProviderCapabilitySupport;
import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogUiBindingResolver;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Appel fournisseur depuis une serviceTask BPMN — délègue aux capabilities INVOKE
 * ({@code GENERATE_OTP}, {@code PAY_BILL}, {@code EXECUTE_TRANSFER}, …) quand résolues.
 */
@Component("gatewayProviderCallDelegate")
public class GatewayProviderCallDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(GatewayProviderCallDelegate.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final CatalogUiBindingResolver uiBindingResolver;
    private final ProviderCallOperationRegistry operationRegistry;
    private final CapabilityDispatcher capabilityDispatcher;
    private final ProviderCapabilitySupport providerCapabilitySupport;

    public GatewayProviderCallDelegate(
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper,
            CatalogUiBindingResolver uiBindingResolver,
            ProviderCallOperationRegistry operationRegistry,
            CapabilityDispatcher capabilityDispatcher,
            ProviderCapabilitySupport providerCapabilitySupport
    ) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.uiBindingResolver = uiBindingResolver;
        this.operationRegistry = operationRegistry;
        this.capabilityDispatcher = capabilityDispatcher;
        this.providerCapabilitySupport = providerCapabilitySupport;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String paymentId = stringVar(execution, PaymentProcessSupport.VAR_PAYMENT_ID);
        if (paymentId == null || paymentId.isBlank()) {
            throw new BusinessException("PAYMENT_ID_MISSING", "paymentId manquant dans le processus");
        }
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable: " + paymentId));

        String activityId = execution.getCurrentActivityId();
        String stepCode = activityId != null ? activityId : "SERVICE";
        Map<String, Object> context = readContext(tx);
        execution.getVariables().forEach((k, v) -> {
            if (v != null && !"otp".equalsIgnoreCase(k)) {
                context.put(k, v);
            }
        });

        CatalogBindingDefinition binding = CatalogStepBindings.merge(
                uiBindingResolver.findInvokeBinding(context, activityId).orElse(null),
                BpmnStepAttributes.of(execution),
                stepCode);
        String operationHint = resolveOperationHint(execution, binding, activityId);

        Map<String, Object> input = new HashMap<>();
        Object otp = execution.getVariable("otp");
        if (otp == null) {
            otp = context.get("otp");
        }
        if (otp != null) {
            input.put("otp", otp);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> catalogConfiguration = context.get("catalogConfiguration") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();

        CapabilityResult capability = capabilityDispatcher.tryExecute(
                paymentId,
                stepCode,
                tx,
                context,
                binding,
                catalogConfiguration,
                input,
                operationHint,
                execution::setVariable
        );

        if (!capability.handled()) {
            // Opération hors mapping capability → invoke générique (compat)
            Map<String, Object> params = new LinkedHashMap<>();
            if (binding != null && binding.params() != null) {
                params.putAll(binding.params());
            }
            CapabilityExecutionContext execCtx = new CapabilityExecutionContext(
                    paymentId, stepCode, tx, context, params, input, binding, execution::setVariable);
            capability = providerCapabilitySupport.invoke(execCtx, operationHint);
            context.put("catalogCapability", "RAW_OPERATION:" + operationHint);
        }

        capabilityDispatcher.applyVars(capability, execution::setVariable);
        for (CapabilityResult.CapabilityEvent ev : capability.events()) {
            addEvent(paymentId, ev.type(), ev.message());
        }
        capabilityDispatcher.applyTerminal(tx, context, capability);
        if (capability.terminal() == CapabilityResult.TerminalEffect.OTP_REQUIRED) {
            String msg = capability.statusMessage();
            throw new BusinessException(
                    "OTP_REQUIRED",
                    msg != null && !msg.isBlank() ? msg : "Code OTP requis avant l'envoi au partenaire");
        }
        tx.setContextJson(writeJson(context));
        transactionRepository.save(tx);
        execution.setVariable("providerSuccess", true);
        log.info("Flowable provider capability ok paymentId={} step={} capability={} opHint={}",
                paymentId, stepCode, context.get("catalogCapability"), operationHint);
    }

    private String resolveOperationHint(
            DelegateExecution execution,
            CatalogBindingDefinition binding,
            String activityId
    ) {
        String operationCode = stringVar(execution, "operationCode");
        if (binding != null && binding.operation() != null
                && (operationCode == null || operationCode.isBlank())) {
            operationCode = binding.operation();
        }
        if (operationCode == null || operationCode.isBlank()) {
            operationCode = operationRegistry.defaultOperationForActivity(activityId);
        }
        return operationCode;
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
