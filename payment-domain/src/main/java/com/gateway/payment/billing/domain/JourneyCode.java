package com.gateway.payment.billing.domain;

/**
 * Parcours métier déclaratif du produit (avant encaissement rail).
 */
public enum JourneyCode {
    /** Mode 1 : saisie → liste factures → sélection → suite. */
    OPEN_BILLS,
    /** Mode 2 : saisie → recherche infos (LOOKUP/GETINFOS) → suite. */
    GET_INFOS,
    /** Mode 3 : saisie (réf. + montant) → suite directe. */
    PREPAID
}
