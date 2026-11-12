package com.gateway.payment.omnicanal.maestro;

import java.util.UUID;

public record OrchestrationPlan(
        UUID schemaId,
        String nomSchema,
        String patternCode,
        String workflowName,
        Integer workflowVersion,
        String engine
) {}
