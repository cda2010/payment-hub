package com.gateway.payment.identity.domain;

/**
 * Personne concernée par une ligne de profil de limite.
 * L'équivalent du canal sur une grille tarifaire.
 */
public enum SensLimite {
    /** Ce que la personne peut envoyer ou retirer. */
    EMETTEUR,
    /** Ce que la personne peut recevoir. */
    BENEFICIAIRE,
    /** Solde maximum sur ses instruments. */
    DETENTION
}
