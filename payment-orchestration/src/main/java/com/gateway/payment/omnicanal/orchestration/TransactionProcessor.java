package com.gateway.payment.omnicanal.orchestration;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.maestro.TransferOrchestrationGateway;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Processeur de transaction (miroir Omnicanal TransactionProcessor) —
 * délègue au maestro SpecDriven.
 */
@Service
public class TransactionProcessor {

    private final TransferOrchestrationGateway orchestrationGateway;

    public TransactionProcessor(TransferOrchestrationGateway orchestrationGateway) {
        this.orchestrationGateway = orchestrationGateway;
    }

    public SpecRunResult process(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            Map<String, String> userParams,
            PaymentTransaction tx
    ) {
        return orchestrationGateway.execute(serviceType, sourceCodeFse, destCodeFse, siege, userParams, tx);
    }
}
