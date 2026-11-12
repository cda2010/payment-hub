package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.TypeEnvoi;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InterfaceUtilisateurResponse(
        UUID id,
        UUID providerId,
        String providerCode,
        String codeFse,
        ServiceTypeCode serviceType,
        String typeMethode,
        TypeEnvoi typeEnvoi,
        UUID providerOperationId,
        String providerOperationCode,
        UUID providerApiId,
        String providerApiCode,
        String codeInterface,
        String nomInterface,
        boolean actif,
        Instant createdAt,
        List<ParametreUtilisateurResponse> parametres
) {}
