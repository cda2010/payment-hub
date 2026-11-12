package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Création / mise à jour d'un corridor inter-FSE (relation + comptes LORO / MIROIR).
 */
public record RelationFseCorridorRequest(
        @NotBlank @Size(max = 20) String fseSource,
        @NotBlank @Size(max = 20) String fseDestination,
        @NotNull ServiceTypeCode serviceType,
        /** Compte pont chez le FSE source (étiquette CPTELORO pour DIFF_FSE). */
        @NotBlank @Size(max = 64) String numeroCompteLoroSource,
        /** Compte pont chez le FSE dest (étiquette CPTEMIROIR pour DIFF_FSE). */
        @NotBlank @Size(max = 64) String numeroCompteMiroirDest,
        /** Si true à la création : crée aussi le corridor inverse avec comptes retaggés. */
        boolean alsoCreateReverse
) {}
