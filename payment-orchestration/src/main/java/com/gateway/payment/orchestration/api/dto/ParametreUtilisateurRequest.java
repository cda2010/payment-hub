package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ProprieteApi;
import jakarta.validation.constraints.NotBlank;

public record ParametreUtilisateurRequest(
        String nomParametre,
        @NotBlank String codeParametre,
        String codeParametreMethode,
        String typeParametre,
        String valeur,
        ProprieteApi parametreApi,
        String attribut,
        String sourceParametre,
        String extractScript,
        String labelKey,
        String labelsJson,
        Boolean affiche,
        Integer ordre
) {}
