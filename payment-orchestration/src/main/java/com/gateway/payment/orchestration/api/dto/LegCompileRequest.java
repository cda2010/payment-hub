package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record LegCompileRequest(
        @NotBlank String sourceCodeFse,
        @NotBlank String destCodeFse,
        @NotBlank String typeMethode,
        @NotNull ProprieteApi apiAUtiliser,
        Map<String, String> userParams,
        UUID ligneSchemaEcritureId,
        CasExecution cas
) {
    public LegCompileRequest(
            String sourceCodeFse,
            String destCodeFse,
            String typeMethode,
            ProprieteApi apiAUtiliser,
            Map<String, String> userParams,
            UUID ligneSchemaEcritureId
    ) {
        this(
                sourceCodeFse,
                destCodeFse,
                typeMethode,
                apiAUtiliser,
                userParams,
                ligneSchemaEcritureId,
                CasExecution.NORMAL
        );
    }
}
