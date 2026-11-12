package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProviderFromTemplateRequest(
        @NotBlank @Size(max = 50) String templateCode,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String libelle,
        String description,
        @NotBlank @Size(max = 50) String environmentCode,
        @NotBlank String baseUrl,
        boolean actif,
        Integer ordreAffichage
) {}
