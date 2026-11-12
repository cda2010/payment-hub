package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RelationFseCorridorResponse(
        UUID id,
        String fseSource,
        String fseDestination,
        ServiceTypeCode serviceType,
        Instant createdAt,
        List<CompteFseItem> comptes
) {
    public record CompteFseItem(
            UUID id,
            String numeroCompte,
            String typeCompteCode,
            String typeCompteLibelle,
            String fseTeneur,
            String fseTitulaire,
            String codeBanque,
            String codeAgence,
            String cle,
            String devise,
            String agenceCode
    ) {}
}
