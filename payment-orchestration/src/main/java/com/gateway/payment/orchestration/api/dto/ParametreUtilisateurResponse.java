package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ProprieteApi;

import java.time.Instant;
import java.util.UUID;

public record ParametreUtilisateurResponse(
        UUID id,
        UUID interfaceUtilisateurId,
        String nomParametre,
        String codeParametre,
        String codeParametreMethode,
        String typeParametre,
        String valeur,
        ProprieteApi parametreApi,
        String attribut,
        String sourceParametre,
        String extractScript,
        String labelKey,
        String labelsJson,
        boolean affiche,
        int ordre,
        Instant createdAt
) {}
