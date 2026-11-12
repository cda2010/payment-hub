package com.gateway.payment.rules.domain;

public enum SourceType {
    TRANSACTION_AMOUNT,
    TRANSACTION_COUNT,
    CUMUL_AMOUNT,
    CUMUL_COUNT,
    WALLET_BALANCE,
    PROJECTED_BALANCE,
    /** Solde détenu + montant de l'opération (crédit). Plafond de détention BCEAO. */
    PROJECTED_HOLDING,
    KYC_LEVEL,
    CUSTOMER_STATUS,
    TRANSACTION_TYPE,
    COUNTRY,
    CHANNEL,
    CUSTOM_ATTRIBUTE
}
