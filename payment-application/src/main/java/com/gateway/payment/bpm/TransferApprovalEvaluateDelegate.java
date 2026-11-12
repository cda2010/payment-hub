package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.capability.CapabilityCodes;
import com.gateway.payment.catalog.capability.CapabilityDispatcher;
import com.gateway.payment.catalog.capability.CapabilityResult;
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
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * ProductStep APPROVAL : capability {@link CapabilityCodes#EVALUATE_APPROVAL}.
 */
@Component("gatewayTransferApprovalEvaluateDelegate")
public class TransferApprovalEvaluateDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(TransferApprovalEvaluateDelegate.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final CapabilityDispatcher capabilityDispatcher;

    public TransferApprovalEvaluateDelegate(
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper,
            CapabilityDispatcher capabilityDispatcher
    ) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.capabilityDispatcher = capabilityDispatcher;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String paymentId = stringVar(execution, PaymentProcessSupport.VAR_PAYMENT_ID);
        if (!StringUtils.hasText(paymentId)) {
            throw new BusinessException("PAYMENT_ID_MISSING", "paymentId manquant dans le processus");
        }
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable: " + paymentId));

        Map<String, Object> context = readContext(tx);
        execution.getVariables().forEach((k, v) -> {
            if (v != null && !"otp".equalsIgnoreCase(k)) {
                context.put(k, v);
            }
        });

        @SuppressWarnings("unchecked")
        Map<String, Object> catalogConfiguration = context.get("catalogConfiguration") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();

        context.put("catalogCapability", CapabilityCodes.EVALUATE_APPROVAL);
        CapabilityResult result = capabilityDispatcher.tryExecute(
                paymentId,
                "EVALUATE_APPROVAL",
                tx,
                context,
                null,
                catalogConfiguration,
                Map.of(),
                CapabilityCodes.EVALUATE_APPROVAL,
                execution::setVariable
        );

        if (!result.handled()) {
            throw new BusinessException("CAPABILITY_MISSING", "Handler EVALUATE_APPROVAL introuvable");
        }

        capabilityDispatcher.applyVars(result, execution::setVariable);
        for (CapabilityResult.CapabilityEvent ev : result.events()) {
            addEvent(paymentId, ev.type(), ev.message());
        }
        capabilityDispatcher.applyTerminal(tx, context, result);
        tx.setContextJson(writeJson(context));
        transactionRepository.save(tx);
        log.info("Approval capability ok paymentId={}", paymentId);
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
