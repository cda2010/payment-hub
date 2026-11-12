package com.gateway.payment.pricing.tarif.domain;

/**
 * Période d'agrégation pour {@link BaseCalculFrais#CUMUL_AMOUNT} / {@link BaseCalculFrais#CUMUL_COUNT}.
 * Aligné sur {@code LimitPeriod} de payment-rules.
 */
public enum PeriodeCumul {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ROLLING_24H,
    ROLLING_30D,
    CUSTOM
}
