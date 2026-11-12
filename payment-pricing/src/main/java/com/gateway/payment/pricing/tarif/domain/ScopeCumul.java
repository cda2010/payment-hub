package com.gateway.payment.pricing.tarif.domain;

/**
 * Périmètre d'agrégation pour un cumul tarifaire.
 * Aligné sur {@code LimitScope} de payment-rules (sous-ensemble utile aux frais).
 */
public enum ScopeCumul {
    CUSTOMER,
    WALLET,
    ACCOUNT,
    AGENT,
    MERCHANT,
    TILL,
    UNITE
}
