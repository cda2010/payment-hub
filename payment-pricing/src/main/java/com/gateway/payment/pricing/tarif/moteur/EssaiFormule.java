package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.validation.ValidationIssue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Résultat d'un essai de formule : soit les erreurs de paramètres, soit le calcul.
 *
 * @param frais frais après plancher/plafond, avant l'arrondi de la grille
 */
public record EssaiFormule(
        boolean valide,
        List<ValidationIssue> erreurs,
        Map<String, Object> parametres,
        BigDecimal baseTaxable,
        BigDecimal fraisFormule,
        String ajustement,
        BigDecimal frais,
        String details
) {

    static EssaiFormule invalide(List<ValidationIssue> erreurs) {
        return new EssaiFormule(false, erreurs, null, null, null, null, null, null);
    }
}
