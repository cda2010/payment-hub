package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Création / mise à jour — champs alignés Omnicanal {@code compte}.
 */
public record CompteFseRequest(
        @NotBlank @Size(max = 20) String numeroCompte,
        @NotBlank @Size(max = 64) String typeCompteCode,
        @NotBlank @Size(max = 20) String fseTeneur,
        @NotBlank @Size(max = 20) String fseTitulaire,
        @NotBlank @Size(max = 20) String codeBanque,
        @Size(max = 20) String codeAgence,
        @Size(max = 5) String cle,
        @Size(max = 10) String devise,
        @Size(max = 200) String libelleCompte,
        @Size(max = 20) String codeUoTitulaire,
        @Size(max = 20) String agenceCode,
        @Size(max = 20) String extype,
        ServiceTypeCode serviceType,
        UUID relationId,
        UUID mirroirId
) {}
