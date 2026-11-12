package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ImputeAgence;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LigneSchemaEcritureRequest(
        @NotNull UUID schemaEcritureId,
        @NotNull Integer ordre,
        String stepId,
        @NotBlank String typeMethode,
        @NotNull ProprieteApi apiAUtiliser,
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
        String criticality
) {}
