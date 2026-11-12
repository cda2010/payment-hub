package com.gateway.payment.catalog.casting;

/**
 * Comment le participant d'un rôle est choisi à l'init du paiement.
 */
public enum CatalogRoleSelection {
    /** L'utilisateur choisit parmi {@code participants[]} du rôle. */
    USER,
    /** Provider imposé (souvent un seul participant). */
    FIXED,
    /** Identique à un autre rôle ({@code derivedFrom}). */
    DERIVED
}
