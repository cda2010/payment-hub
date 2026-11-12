package com.gateway.payment.common.domain;

/**
 * Catégorie métier du moyen de paiement (orthogonale à {@link ExecutionMode}).
 */
public enum PaymentMethodType {
    MOBILE_MONEY,
    CARD,
    BANK_TRANSFER,
    /** Produits orchestrés (transfert multi-rails, factures) — hors checkout marchand. */
    OTHER
}
