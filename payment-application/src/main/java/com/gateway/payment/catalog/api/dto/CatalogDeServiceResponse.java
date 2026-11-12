package com.gateway.payment.catalog.api.dto;

import com.gateway.payment.catalog.domain.CatalogDeServiceStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CatalogDeServiceResponse(
        UUID id,
        String code,
        /** Résolu selon {@code Accept-Language}, repli sur la colonne d'origine. */
        String name,
        String description,
        /** Traductions brutes pour l'admin : {@code {lang → texte}}, null si aucune. */
        Map<String, String> nameI18n,
        Map<String, String> descriptionI18n,
        UUID categoryId,
        String categoryCode,
        String categoryName,
        CatalogDeServiceStatus status,
        UUID currentVersionId,
        String currentVersion,
        Instant createdAt,
        Instant updatedAt
) {}
