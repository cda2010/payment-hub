package com.gateway.payment.catalog.api.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CategorieServiceResponse(
        UUID id,
        String code,
        /** Résolu selon {@code Accept-Language}, repli sur la colonne d'origine. */
        String name,
        String description,
        Map<String, String> nameI18n,
        Map<String, String> descriptionI18n,
        Integer ordreAffichage,
        boolean actif,
        Instant createdAt,
        Instant updatedAt
) {}
