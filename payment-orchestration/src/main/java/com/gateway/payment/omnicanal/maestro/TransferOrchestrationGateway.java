package com.gateway.payment.omnicanal.maestro;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.response.OrchErrorCode;
import com.gateway.payment.omnicanal.response.OrchestrationException;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.omnicanal.schema.WorkflowSelectService;
import com.gateway.payment.omnicanal.spec.SpecDrivenTransferExecutor;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Gateway d'orchestration (miroir Omnicanal TransferOrchestrationGateway).
 * Route vers SpecDriven (schéma) — Flowable same_fse/inter_fse reste via BPMN checkout/transfer.
 */
@Service
public class TransferOrchestrationGateway {

    private static final Logger log = LoggerFactory.getLogger(TransferOrchestrationGateway.class);

    private final WorkflowSelectService workflowSelectService;
    private final SpecDrivenTransferExecutor specDrivenTransferExecutor;

    public TransferOrchestrationGateway(
            WorkflowSelectService workflowSelectService,
            SpecDrivenTransferExecutor specDrivenTransferExecutor
    ) {
        this.workflowSelectService = workflowSelectService;
        this.specDrivenTransferExecutor = specDrivenTransferExecutor;
    }

    public SpecRunResult execute(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            Map<String, String> userParams,
            PaymentTransaction tx
    ) {
        OrchestrationPlan plan = workflowSelectService.resolve(serviceType, sourceCodeFse, destCodeFse, siege);
        log.info(
                "Maestro plan serviceType={} workflow={} schemaId={} pattern={}",
                serviceType, plan.workflowName(), plan.schemaId(), plan.patternCode()
        );
        if (plan.schemaId() == null) {
            throw new OrchestrationException(
                    OrchErrorCode.ORCH_NO_SCHEMA,
                    "Aucun schéma pour " + serviceType + " " + sourceCodeFse + "→" + destCodeFse,
                    null
            );
        }
        return specDrivenTransferExecutor.execute(new SpecDrivenTransferExecutor.SpecDrivenRequest(
                serviceType, sourceCodeFse, destCodeFse, siege, plan.schemaId(), userParams, tx
        ));
    }
}
