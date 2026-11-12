package com.gateway.payment.payment.application;

import com.gateway.payment.payment.domain.PaymentExecution;
import com.gateway.payment.payment.repository.PaymentExecutionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistance audit hors du hot path (appels connecteur).
 */
@Service
public class PaymentExecutionAuditWriter {

    private final PaymentExecutionRepository executionRepository;

    public PaymentExecutionAuditWriter(PaymentExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Async("paymentAuditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAsync(PaymentExecution execution) {
        executionRepository.save(execution);
    }
}
