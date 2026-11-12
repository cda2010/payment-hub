package com.gateway.payment.pricing.tarif.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

/**
 * Source ou destination visée par une grille : toutes (aucune colonne renseignée), une zone (zone seule) ou une
 * unité d'organisation de n'importe quel type, regroupement compris (zone de l'unité recopiée). Code, libellé et
 * type de l'unité sont une copie pour l'affichage : l'unité vit dans payment-network. Le parent de l'unité, copié aussi,
 * sert seulement à repérer les grilles qui visent des unités sœurs.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CibleReseau {

    @Column(length = 10)
    private String zone;

    @Column(name = "unite_id")
    private UUID uniteId;

    @Column(name = "unite_code", length = 40)
    private String uniteCode;

    @Column(name = "unite_libelle", length = 150)
    private String uniteLibelle;

    @Column(name = "unite_type", length = 20)
    private String uniteType;

    @Column(name = "unite_parent_id")
    private UUID uniteParentId;

    public static CibleReseau zone(String zone) {
        return new CibleReseau(zone, null, null, null, null, null);
    }

    /** Hibernate relit une cible dont toutes les colonnes sont vides comme {@code null}. */
    public static boolean estVide(CibleReseau c) {
        return c == null || c.zone == null;
    }

    public static Niveau niveau(CibleReseau c) {
        if (estVide(c)) {
            return Niveau.TOUTES;
        }
        return c.uniteId == null ? Niveau.ZONE : Niveau.UNITE;
    }

    /** Même cible : même zone et même unité (la copie d'affichage n'entre pas en compte). */
    public static boolean memeCible(CibleReseau a, CibleReseau b) {
        if (estVide(a) || estVide(b)) {
            return estVide(a) && estVide(b);
        }
        return a.zone.equals(b.zone) && Objects.equals(a.uniteId, b.uniteId);
    }

    public static CibleReseau copie(CibleReseau c) {
        return estVide(c) ? null
                : new CibleReseau(c.zone, c.uniteId, c.uniteCode, c.uniteLibelle, c.uniteType, c.uniteParentId);
    }

    /** Libellé court : « toutes », la zone ou le code de l'unité. */
    public static String texte(CibleReseau c) {
        return switch (niveau(c)) {
            case TOUTES -> "toutes";
            case ZONE -> "zone " + c.zone;
            case UNITE -> c.uniteCode;
        };
    }

    public enum Niveau { TOUTES, ZONE, UNITE }
}
