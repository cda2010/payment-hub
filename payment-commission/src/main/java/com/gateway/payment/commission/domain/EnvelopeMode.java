package com.gateway.payment.commission.domain;

public enum EnvelopeMode {
    /** 100 % des frais répartis entre les règles. */
    FULL_FEES,
    /** Répartition partielle ; reliquat possible. */
    PARTIAL_FEES,
    /** Commissions indépendantes des frais. */
    INDEPENDENT,
    MIXED
}
