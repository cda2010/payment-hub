package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InterfaceUtilisateurRequest(
        @NotBlank String codeFse,
        @NotNull ServiceTypeCode serviceType,
        @NotBlank String typeMethode,
        TypeEnvoi typeEnvoi,
        UUID providerOperationId,
        UUID providerApiId,
        String codeInterface,
        String nomInterface,
        boolean actif
) {}
