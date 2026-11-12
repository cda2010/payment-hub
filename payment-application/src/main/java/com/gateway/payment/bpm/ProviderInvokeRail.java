package com.gateway.payment.bpm;

/**
 * Rail fournisseur pour un appel provider dans un parcours composé.
 */
public enum ProviderInvokeRail {
    /** Rail facturier ({@code billProviderCode} / rôle BILLER). */
    BILLER,
    /** Rail wallet / payer ({@code payProviderCode} / rôle PAYER). */
    PAYER,
    /** Provider principal de la transaction. */
    PRIMARY
}
