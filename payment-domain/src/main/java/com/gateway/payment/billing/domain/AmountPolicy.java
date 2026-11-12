package com.gateway.payment.billing.domain;

/** Politique de montant acceptée pour un produit facturier. */
public enum AmountPolicy {
    /** Montant dû exact uniquement. */
    FULL_ONLY,
    /** Paiement partiel autorisé (≤ montant dû, ≥ min). */
    PARTIAL,
    /** Montant libre (ex. prépayé / top-up), borné par min/max. */
    EDITABLE
}

