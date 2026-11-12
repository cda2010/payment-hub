package com.gateway.payment.configuration.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Création d'une Méthode au format Omnicanal → liée à une Api (fseApiId).
 */
public record OmnicanalMethodCreateRequest(
        /** Api Omnicanal parente (obligatoire). */
        @NotNull UUID fseApiId,
        /** TypeMethode Omnicanal (ex. CONSULTATIONSOLDE, TRANSFERT). Devient le code opération. */
        @NotBlank @Size(max = 50) String typeMethode,
        /** Code méthode technique Omnicanal (ex. 000703). */
        @Size(max = 50) String codeMethode,
        /** Path HTTP = nomMethode Omnicanal (ex. api/gateway/solde). */
        @NotBlank String nomMethode,
        /** GET / POST / PUT / DELETE (= typeAppel). */
        @NotBlank String typeAppel,
        /** JSON / XML (= typeReponse) — informatif. */
        String typeReponse,
        boolean actif,
        boolean idempotent,
        @Valid List<OmnicanalParamCreateRequest> parametres
) {
    public record OmnicanalParamCreateRequest(
            /** code_parametre interne (variable ${...}). */
            @NotBlank String codeParametre,
            /** Clé JSON / query outbound (= nom_externe Omnicanal). */
            @NotBlank String nomExterne,
            String typeParametre,
            String categorie
    ) {}
}
