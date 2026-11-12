package com.gateway.payment.pricing.tarif.domain;

/**
 * Assiette d'entrée des frais sur une grille.
 * Distinct de la « base taxable » interne des formules {@code EXCESS_*} (montant − seuil).
 */
public enum BaseCalculFrais {
    /** Principal de l'opération ({@code amount}) — défaut. */
    TRANSACTION_AMOUNT,
    /** Montant après conversion FX. */
    CONVERTED_AMOUNT,
    /** Volume cumulé sur une période / un scope (agrégat fourni par l'appelant). */
    CUMUL_AMOUNT,
    /** Nombre d'opérations cumulé sur une période / un scope. */
    CUMUL_COUNT,
    /** Scalaire fourni explicitement ({@code feeBaseAmount}). */
    CUSTOM
}
