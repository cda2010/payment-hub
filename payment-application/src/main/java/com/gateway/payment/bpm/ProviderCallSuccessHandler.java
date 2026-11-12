package com.gateway.payment.bpm;

/**
 * Effet post-succès d'un appel fournisseur (statut / événements).
 */
public enum ProviderCallSuccessHandler {
    /** Aucun changement de statut dédié (ex. LOOKUP avant userTask). */
    NONE,
    /** OTP généré → {@code OTP_REQUIRED}. */
    OTP_REQUIRED,
    /** Fin de parcours → {@code SUCCESS}. */
    SUCCESS_TERMINAL,
    /**
     * Confirm terminal si {@code terminalOnConfirm=true}, sinon débit wallet
     * intermédiaire (facture composée).
     */
    CONFIRM_OR_CONTINUE,
    /** Redirect URL présente → REDIRECT_REQUIRED, sinon WAITING_CALLBACK. */
    REDIRECT_OR_WAIT,
    /** Facture consultée (événement audit). */
    BILL_LOOKED_UP,
    /** Liste de factures chargée (événement audit). */
    BILL_LISTED
}
