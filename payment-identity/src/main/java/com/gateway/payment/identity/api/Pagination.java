package com.gateway.payment.identity.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Pagination des listes Identity : page à partir de 0, au plus {@value #TAILLE_MAX} résultats.
 */
final class Pagination {

    static final int TAILLE_DEFAUT = 50;
    static final int TAILLE_MAX = 500;

    private static final Map<String, String> COLONNES_PARTY = Map.of(
            "code", "code",
            "displayname", "displayName",
            "kind", "kind",
            "phone", "phone",
            "email", "email",
            "tenantid", "tenantId",
            "active", "active",
            "maj", "updatedAt");

    private Pagination() {
    }

    static Pageable parties(Integer page, Integer taille, String tri, String sens) {
        int p = page == null || page < 0 ? 0 : page;
        int t = taille == null || taille < 1 ? TAILLE_DEFAUT : Math.min(taille, TAILLE_MAX);
        return PageRequest.of(p, t, ordreParty(tri, sens));
    }

    private static Sort ordreParty(String tri, String sens) {
        String colonne = tri == null ? null : COLONNES_PARTY.get(tri.trim().toLowerCase().replace("_", ""));
        if (colonne == null) {
            return Sort.by("displayName", "code");
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sens) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, colonne);
        return colonne.equals("code") ? sort : sort.and(Sort.by("code"));
    }
}
