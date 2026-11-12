package com.gateway.payment.omnicanal.schema;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.maestro.OrchestrationPlan;
import com.gateway.payment.orchestration.api.dto.SchemaMatchRequest;
import com.gateway.payment.orchestration.api.dto.SchemaMatchResponse;
import com.gateway.payment.orchestration.service.SchemaMatchingService;
import org.springframework.stereotype.Service;

/**
 * Sélection de workflow via matching DMN schéma (miroir Omnicanal WorkflowSelectService).
 */
@Service
public class WorkflowSelectService {

    private final SchemaMatchingService matchingService;

    public WorkflowSelectService(SchemaMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public OrchestrationPlan resolve(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege
    ) {
        SchemaMatchResponse matched = matchingService.match(new SchemaMatchRequest(
                serviceType != null ? serviceType : ServiceTypeCode.TRANSFERT,
                sourceCodeFse,
                destCodeFse,
                siege
        ));
        return new OrchestrationPlan(
                matched.schemaId(),
                matched.nomSchema(),
                matched.patternCode(),
                matched.workflowName(),
                matched.workflowVersion(),
                "flowable"
        );
    }
}
