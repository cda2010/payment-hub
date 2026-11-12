package com.gateway.payment.pricing.tarif.moteur;

import java.util.UUID;

/**
 * Où se situe un côté de l'opération (source ou destination). Le critère le plus précis l'emporte : l'unité,
 * puis le pays (code ISO), puis la zone. Tout vide : côté inconnu ou sans objet.
 */
public record Localisation(UUID uniteId, String pays, String zone) {

    public static final Localisation AUCUNE = new Localisation(null, null, null);

    public boolean estVide() {
        return uniteId == null && vide(pays) && vide(zone);
    }

    public String texte() {
        if (uniteId != null) {
            return "unité " + uniteId;
        }
        if (!vide(pays)) {
            return "pays " + pays.trim().toUpperCase();
        }
        return vide(zone) ? "non renseignée" : "zone " + zone.trim().toUpperCase();
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
