package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ImputeAgence;
import com.gateway.payment.orchestration.domain.ProprieteApi;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LigneSchemaEcritureResponse(
        UUID id,
        UUID schemaEcritureId,
        Integer ordre,
        String stepId,
        String typeMethode,
        ProprieteApi apiAUtiliser,
        ImputeAgence agenceImputer,
        String executionMode,
        String timeoutPolicy,
        String timeoutDuration,
        String callbackPolicy,
        String statusMethod,
        String retryPolicy,
        Integer retryMaxAttempts,
        Long retryInitialDelayMs,
        Double retryMultiplier,
        Long retryMaxDelayMs,
        String retryableCodes,
        String idempotencyPolicy,
        String compensationPolicy,
        String compensationMode,
        String compensationStepCode,
        Boolean compensationReverseDesc,
        Integer compensationMaxAttempts,
        Boolean compensationManualReview,
        String dependsOnStepCode,
        String dependencyCondition,
        Boolean dependencyRequired,
        String errorPolicies,
        String errorPolicyCode,
        String defaultErrorAction,
        String checkStatusMethodCode,
        Integer maxRetries,
        Boolean blocking,
        String criticality,
        Instant createdAt,
        List<ParametreSchemaResponse> parametres
) {}
