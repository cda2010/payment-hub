package com.gateway.payment.commission.domain;

/** Base numérique d'entrée du calcul (CDC § sources). */
public enum SourceCommission {
    TRANSACTION_AMOUNT,
    TRANSACTION_FEES,
    CUMUL_AMOUNT,
    CUMUL_FEES,
    TRANSACTION_COUNT,
    NET_REVENUE
}
