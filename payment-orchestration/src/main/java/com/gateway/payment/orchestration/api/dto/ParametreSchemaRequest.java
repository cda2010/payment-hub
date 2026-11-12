package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.orchestration.domain.ParametreCategorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ParametreSchemaRequest(
        @NotNull UUID ligneSchemaEcritureId,
        String nomParametre,
        @NotBlank String codeParametre,
        String codeParametreMethode,
        String nomExterne,
        String typeParametre,
        ParametreCategorie categorie,
        String valeurDefaut
) {}
