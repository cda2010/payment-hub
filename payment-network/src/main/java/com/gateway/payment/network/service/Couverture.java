package com.gateway.payment.network.service;

import com.gateway.payment.network.domain.UniteOrganisation;

import java.util.List;
import java.util.Optional;

/**
 * Un regroupement couvre chaque membre et toute sa descendance : deux membres dont l'un descend de l'autre
 * couvriraient la même unité deux fois.
 */
final class Couverture {

    /** Unité et chemin à contrôler (le chemin peut être celui qu'elle aurait après un déplacement). */
    record Position(UniteOrganisation unite, String chemin) {
    }

    record Chevauchement(UniteOrganisation ascendant, UniteOrganisation descendant) {
    }

    private Couverture() {
    }

    static Optional<Chevauchement> premierChevauchement(List<Position> positions) {
        for (Position a : positions) {
            for (Position b : positions) {
                if (a != b && b.chemin().startsWith(a.chemin()) && !b.chemin().equals(a.chemin())) {
                    return Optional.of(new Chevauchement(a.unite(), b.unite()));
                }
            }
        }
        return Optional.empty();
    }
}
