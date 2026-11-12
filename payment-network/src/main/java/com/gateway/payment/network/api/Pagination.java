package com.gateway.payment.network.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Pagination des listes d'unités : page à partir de 0, au plus {@value #TAILLE_MAX} résultats par page. Le tri se
 * fait sur une colonne connue, le code départageant les égalités ; une colonne inconnue revient au tri par défaut
 * (niveau puis code).
 */
final class Pagination {

    static final int TAILLE_DEFAUT = 50;
    static final int TAILLE_MAX = 500;

    private static final Map<String, String> COLONNES = Map.of(
            "code", "code",
            "libelle", "libelle",
            "type", "type",
            "zone", "zone",
            "statut", "statut",
            "niveau", "niveau",
            "maj", "updatedAt");

    private Pagination() {
    }

    static Pageable de(Integer page, Integer taille, String tri, String sens) {
        int p = page == null || page < 0 ? 0 : page;
        int t = taille == null || taille < 1 ? TAILLE_DEFAUT : Math.min(taille, TAILLE_MAX);
        return PageRequest.of(p, t, ordre(tri, sens));
    }

    private static Sort ordre(String tri, String sens) {
        String colonne = tri == null ? null : COLONNES.get(tri.trim().toLowerCase());
        if (colonne == null) {
            return Sort.by("niveau", "code");
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sens) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, colonne);
        return colonne.equals("code") ? sort : sort.and(Sort.by("code"));
    }
}
