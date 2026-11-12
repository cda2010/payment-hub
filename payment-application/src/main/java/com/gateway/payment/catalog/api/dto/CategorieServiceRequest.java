package com.gateway.payment.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CategorieServiceRequest(
        @NotBlank @Size(max = 50) String code,
        /** Libellé dans la langue par défaut ({@code gateway.i18n.default-language}). */
        @NotBlank @Size(max = 150) String name,
        String description,
        Map<String, String> nameI18n,
        Map<String, String> descriptionI18n,
        Integer ordreAffichage,
        Boolean actif
) {}
