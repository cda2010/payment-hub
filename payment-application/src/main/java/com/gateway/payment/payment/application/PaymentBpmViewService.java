package com.gateway.payment.payment.application;

import com.gateway.payment.bpm.PaymentProcessSupport;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.rapidframe.bpm.service.BpmProcessExecutionViewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * État d'exécution Flowable d'un paiement pour le viewer BPMN (admin détail transaction).
 */
@Service
public class PaymentBpmViewService {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentProcessSupport processSupport;
    private final BpmProcessExecutionViewService executionViewService;

    public PaymentBpmViewService(
            PaymentTransactionRepository transactionRepository,
            PaymentProcessSupport processSupport,
            BpmProcessExecutionViewService executionViewService
    ) {
        this.transactionRepository = transactionRepository;
        this.processSupport = processSupport;
        this.executionViewService = executionViewService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> diagramState(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new BusinessException("PAYMENT_NOT_FOUND", "paymentId manquant", HttpStatus.NOT_FOUND);
        }
        String id = paymentId.trim();
        if (!transactionRepository.findByPaymentId(id).isPresent()) {
            throw new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable: " + id, HttpStatus.NOT_FOUND);
        }
        String processInstanceId = processSupport.findRootProcessInstanceId(id);
        if (processInstanceId == null) {
            throw new BusinessException(
                    "BPM_INSTANCE_NOT_FOUND",
                    "Aucune instance Flowable pour ce paiement",
                    HttpStatus.NOT_FOUND
            );
        }
        try {
            Map<String, Object> state = executionViewService.getDiagramState(processInstanceId);
            if (state == null) {
                throw new BusinessException(
                        "BPM_INSTANCE_NOT_FOUND",
                        "État Flowable introuvable",
                        HttpStatus.NOT_FOUND
                );
            }
            Map<String, Object> out = new HashMap<>(state);
            out.put("paymentId", id);
            return out;
        } catch (IOException ex) {
            throw new BusinessException(
                    "BPM_DIAGRAM_ERROR",
                    ex.getMessage() != null ? ex.getMessage() : "Erreur diagramme BPMN",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
