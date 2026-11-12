package com.gateway.payment.orchestration.api.dto;

import java.util.List;

public record OrchestrationCatalogResponse(
        List<String> patternCodes,
        List<String> typeMethodes,
        List<String> executionModes,
        List<String> timeoutPolicies,
        List<String> callbackPolicies,
        List<String> retryPolicies,
        List<String> idempotencyPolicies,
        List<String> compensationPolicies,
        List<String> compensationModes,
        List<String> criticalities,
        List<String> errorActions,
        List<String> dependencyConditions,
        List<String> paramTypes
) {}
