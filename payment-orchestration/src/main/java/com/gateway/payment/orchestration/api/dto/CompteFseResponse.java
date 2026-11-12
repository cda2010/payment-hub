package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.time.Instant;
import java.util.UUID;

public record CompteFseResponse(
        UUID id,
        Long omniId,
        String numeroCompte,
        String codeBanque,
        String codeAgence,
        String cle,
        String extype,
        String typeCompteCode,
        String typeCompteLibelle,
        String devise,
        String fseTeneur,
        String fseTitulaire,
        ServiceTypeCode serviceType,
        String libelleCompte,
        String codeUoTitulaire,
        UUID mirroirId,
        String agenceCode,
        UUID relationId,
        String fseSource,
        String fseDestination,
        ServiceTypeCode relationServiceType,
        Instant createdAt
) {}
