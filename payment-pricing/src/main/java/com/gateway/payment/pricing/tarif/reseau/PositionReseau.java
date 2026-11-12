package com.gateway.payment.pricing.tarif.reseau;

import java.util.List;
import java.util.UUID;

/**
 * Position d'une unité, d'un pays ou d'une zone, telle que la renvoie payment-network
 * ({@code GET /api/v1/network/positions}).
 *
 * @param unite         vide pour une zone
 * @param lignee        l'unité puis ses ascendants jusqu'au pays ; vide pour une zone
 * @param regroupements regroupements qui couvrent l'unité, avec le membre par lequel ils la couvrent
 */
public record PositionReseau(String zone, String devise, Unite unite, List<Unite> lignee,
                             List<Couverture> regroupements) {

    public PositionReseau {
        lignee = lignee == null ? List.of() : List.copyOf(lignee);
        regroupements = regroupements == null ? List.of() : List.copyOf(regroupements);
    }

    public record Unite(UUID id, String code, String libelle, String type, String zone, String statut, int niveau) {

        public boolean estActive() {
            return "ACTIF".equals(statut);
        }

        public boolean estRegroupement() {
            return "REGROUPEMENT".equals(type);
        }
    }

    public record Couverture(Unite regroupement, Unite via, boolean direct) {
    }
}
