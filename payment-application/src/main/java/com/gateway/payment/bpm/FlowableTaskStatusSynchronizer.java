package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Synchronise statut / étape transaction après une transition Flowable
 * (userTask active → {@link PaymentStatus}).
 */
@Component
public class FlowableTaskStatusSynchronizer {

    private final PaymentProcessSupport processSupport;
    private final BpmCheckoutNextActionService nextActionService;
    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public FlowableTaskStatusSynchronizer(
            PaymentProcessSupport processSupport,
            BpmCheckoutNextActionService nextActionService,
            PaymentTransactionRepository transactionRepository,
            ObjectMapper objectMapper
    ) {
        this.processSupport = processSupport;
        this.nextActionService = nextActionService;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    public void syncAfterTransition(PaymentTransaction tx) {
        PaymentTransaction fresh = transactionRepository.findByPaymentId(tx.getPaymentId()).orElse(tx);
        copyState(fresh, tx);

        if (!processSupport.isAlive(tx.getPaymentId())) {
            if (tx.getStatus() != PaymentStatus.FAILED && tx.getStatus() != PaymentStatus.CANCELLED) {
                if (tx.getStatus() != PaymentStatus.SUCCESS) {
                    tx.applyStatus(PaymentStatus.SUCCESS);
                    tx.setCompletedAt(Instant.now());
                    tx.setCurrentStep("SUCCESS");
                }
            }
            transactionRepository.save(tx);
            return;
        }

        Task task = processSupport.findActiveTask(tx.getPaymentId(), null);
        if (task == null) {
            if (tx.getStatus() == PaymentStatus.INITIATED || tx.getStatus() == PaymentStatus.PROCESSING) {
                tx.applyStatus(PaymentStatus.PENDING);
            }
            transactionRepository.save(tx);
            return;
        }

        tx.setCurrentStep(task.getTaskDefinitionKey());
        Map<String, Object> ctx = readContext(tx);
        if (task.getFormKey() != null) {
            ctx.put("activeFormKey", task.getFormKey());
            tx.setContextJson(writeJson(ctx));
        }

        String formKey = task.getFormKey();
        String taskKey = task.getTaskDefinitionKey();
        if (nextActionService.isOtpTask(formKey, taskKey)) {
            tx.applyStatus(PaymentStatus.OTP_REQUIRED);
        } else if (nextActionService.isApprovalTask(formKey, taskKey)) {
            tx.applyStatus(PaymentStatus.WAITING_APPROVAL);
            ctx.put("waitMessage", "Transfert en attente d'approbation");
            tx.setContextJson(writeJson(ctx));
        } else if (nextActionService.isReturnTask(formKey, taskKey)) {
            tx.applyStatus(PaymentStatus.RETURNED_FOR_CORRECTION);
            if (ctx.get("waitMessage") == null) {
                ctx.put("waitMessage", "Approbation refusée — correction requise");
                tx.setContextJson(writeJson(ctx));
            }
        } else if (nextActionService.isWaitTask(formKey, taskKey)) {
            tx.applyStatus(ctx.get("redirectUrl") != null
                    ? PaymentStatus.REDIRECT_REQUIRED
                    : PaymentStatus.WAITING_CALLBACK);
        } else {
            tx.applyStatus(PaymentStatus.AWAITING_USER_INPUT);
        }
        transactionRepository.save(tx);
    }

    private void copyState(PaymentTransaction from, PaymentTransaction to) {
        to.hydrateStatus(from.getStatus());
        to.setCurrentStep(from.getCurrentStep());
        to.setContextJson(from.getContextJson());
        to.setProviderReference(from.getProviderReference());
        to.setErrorCode(from.getErrorCode());
        to.setErrorMessage(from.getErrorMessage());
        to.setCompletedAt(from.getCompletedAt());
        to.setPhoneNumber(from.getPhoneNumber());
        to.setAmount(from.getAmount());
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
}
