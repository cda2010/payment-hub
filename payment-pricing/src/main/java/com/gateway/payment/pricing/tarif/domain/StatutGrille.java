package com.gateway.payment.pricing.tarif.domain;

/** Cycle de vie d'une version de grille (cahier des charges §11). */
public enum StatutGrille {
    BROUILLON,
    A_VALIDER,
    VALIDEE,
    PUBLIEE,
    DESACTIVEE
}
