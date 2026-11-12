package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais.Correspondance;
import com.gateway.payment.pricing.tarif.reseau.PositionReseau;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Correspondance entre la cible d'une grille (source ou destination) et la position de l'opération, avec son rang
 * de précision : plus il est petit, plus la grille est précise. Pour une lignée de L unités (l'unité de l'opération
 * à l'indice 0, puis ses ascendants jusqu'au pays) :
 * <ul>
 *   <li>unité de la lignée à l'indice i : 2i (l'unité elle-même vaut 0) ;</li>
 *   <li>regroupement qui couvre l'opération par le membre d'indice j : 2j + 1, juste après ce membre et avant son
 *       parent (le regroupement ne contient que ce membre et quelques autres unités, le parent toute sa branche) ;</li>
 *   <li>zone de l'opération : 2L ;</li>
 *   <li>« toutes » : le moins précis.</li>
 * </ul>
 */
final class Ciblage {

    static final int TOUTES = Integer.MAX_VALUE;

    private Ciblage() {
    }

    record Rang(int valeur, Correspondance correspondance) {
    }

    /** Vide si la grille ne vise pas cette position (ou si la position est inconnue et la cible renseignée). */
    static Optional<Rang> rang(CibleReseau cible, PositionReseau position) {
        if (CibleReseau.estVide(cible)) {
            return Optional.of(new Rang(TOUTES, Correspondance.TOUTES));
        }
        if (position == null) {
            return Optional.empty();
        }
        List<PositionReseau.Unite> lignee = position.lignee();
        if (cible.getUniteId() == null) {
            return cible.getZone().equals(position.zone())
                    ? Optional.of(new Rang(2 * lignee.size(), new Correspondance(CibleReseau.Niveau.ZONE, cible.getZone(), null)))
                    : Optional.empty();
        }
        int i = indice(lignee, cible.getUniteId());
        if (i >= 0) {
            return Optional.of(new Rang(2 * i, new Correspondance(CibleReseau.Niveau.UNITE, cible.getUniteCode(), null)));
        }
        return position.regroupements().stream()
                .filter(c -> c.regroupement().id().equals(cible.getUniteId()))
                .map(c -> new Rang(2 * indice(lignee, c.via().id()) + 1,
                        new Correspondance(CibleReseau.Niveau.UNITE, cible.getUniteCode(), c.via().code())))
                .filter(r -> r.valeur() > 0)
                .min((a, b) -> Integer.compare(a.valeur(), b.valeur()));
    }

    private static int indice(List<PositionReseau.Unite> lignee, UUID id) {
        for (int i = 0; i < lignee.size(); i++) {
            if (lignee.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
