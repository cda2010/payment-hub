package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ParametreCategorie;

import java.time.Instant;
import java.util.UUID;

public record ParametreSchemaResponse(
        UUID id,
        UUID ligneSchemaEcritureId,
        String nomParametre,
        String codeParametre,
        String codeParametreMethode,
        String nomExterne,
        String typeParametre,
        ParametreCategorie categorie,
        String valeurDefaut,
        Instant createdAt
) {}
