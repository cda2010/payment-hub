package com.gateway.payment.configuration.domain;

/**
 * Type / nature du fournisseur de service (distinct de {@code PaymentMethodType}
 * et du {@code ServiceTypeCode}).
 * <ul>
 *   <li>{@link #WALLET} — mobile money / portefeuille</li>
 *   <li>{@link #BANQUE} — établissement bancaire / FSE banque</li>
 *   <li>{@link #FACTURIER} — émetteur de factures (LOOKUP + acquittement)</li>
 *   <li>{@link #REMITTANCE} — opérateur de transfert d'argent (RIA, Western Union…)</li>
 *   <li>{@link #RESEAU} — rail / réseau (SWIFT, STAR, RTGS, agrégateur)</li>
 * </ul>
 */
public enum FseParticipantKind {
    WALLET,
    BANQUE,
    FACTURIER,
    REMITTANCE,
    RESEAU
}
