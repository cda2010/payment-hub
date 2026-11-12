package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ProprieteApi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record LegExecuteRequest(
        @NotBlank String sourceCodeFse,
        @NotBlank String destCodeFse,
        @NotBlank String typeMethode,
        @NotNull ProprieteApi apiAUtiliser,
        Map<String, String> userParams,
        UUID ligneSchemaEcritureId,
        String providerCode,
        String paymentId
) {}
