package com.gateway.payment.pricing.tarif.validation;

/**
 * Erreur de configuration localisée (EF-015).
 *
 * @param tranche ordre de la tranche en cause, vide pour une erreur de grille
 * @param champ   champ en cause, ex. {@code montantMax} ou {@code parametres.taux}
 */
public record ValidationIssue(String code, String message, Integer tranche, String champ) {

    public static ValidationIssue grille(String code, String champ, String message) {
        return new ValidationIssue(code, message, null, champ);
    }

    public static ValidationIssue tranche(Integer ordre, String code, String champ, String message) {
        return new ValidationIssue(code, message, ordre, champ);
    }
}
