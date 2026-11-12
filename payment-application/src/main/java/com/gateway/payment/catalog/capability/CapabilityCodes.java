package com.gateway.payment.catalog.capability;

/**
 * Codes capability (actions réutilisables) — <strong>pas</strong> des codes produit catalogue.
 * <p>
 * Produits : {@code ACCOUNT_TRANSFER}, {@code BILL_PAYMENT}, {@code CHECKOUT}, {@code CASH_IN}, …
 * Capabilities : verbes / briques ({@code EXECUTE_TRANSFER}, {@code PAY_BILL}, {@code NOTIFY}, …).
 */
public final class CapabilityCodes {

    // ── Locales (SPI actif) ─────────────────────────────────────────────────
    public static final String VALIDATE_AMOUNT = "VALIDATE_AMOUNT";
    public static final String VALIDATE_ACCOUNT = "VALIDATE_ACCOUNT";
    public static final String CALCULATE_FEES = "CALCULATE_FEES";
    /** Évaluation plafonds / cumuls via payment-rules (réserve si ALLOW). */
    public static final String EVALUATE_RULES = "EVALUATE_RULES";
    public static final String GENERATE_CODE = "GENERATE_CODE";
    public static final String NOTIFY = "NOTIFY";
    public static final String WAIT = "WAIT";
    public static final String APPROVAL_OUTCOME = "APPROVAL_OUTCOME";
    public static final String SOFT_RULES = "SOFT_RULES";
    /** DMN montant/provider → profil de pièces ({@code documentProfileCode}). */
    public static final String RESOLVE_DOCUMENTS = "RESOLVE_DOCUMENTS";
    /** Vérifie présence des pièces requises dans {@code context.documents}. */
    public static final String VALIDATE_DOCUMENTS = "VALIDATE_DOCUMENTS";

    // ── Provider / INVOKE (réservés — ne pas confondre avec les produits) ───
    public static final String GET_INFOS = "GET_INFOS";
    /**
     * Recherche titulaire chez le FSE (wallet MSISDN / compte banque).
     * Opération Omnicanal : {@code INFOCLIENT}. Produit associé : {@code ADD_BENEFICIARY}.
     */
    public static final String INFO_CLIENT = "INFO_CLIENT";
    /**
     * Enregistrement carnet après recherche — capability locale (pas d'appel partenaire).
     */
    public static final String SAVE_TRANSFER_BENEFICIARY = "SAVE_TRANSFER_BENEFICIARY";
    public static final String GET_BILLS = "GET_BILLS";
    public static final String GET_BALANCE = "GET_BALANCE";
    /** Crédit wallet / airtime — pas le nom d’un produit catalogue. */
    public static final String EXECUTE_TOPUP = "EXECUTE_TOPUP";
    /** Paiement facture (produit associé : {@code BILL_PAYMENT}). */
    public static final String PAY_BILL = "PAY_BILL";
    /** Exécution transfert (produits : {@code ACCOUNT_TRANSFER}, {@code RTGS}, {@code SWIFT}…). */
    public static final String EXECUTE_TRANSFER = "EXECUTE_TRANSFER";
    /** Débit source cash-out (produit : {@code CASH_OUT}) — rail SpecDriven {@code TRANSFERT}. */
    public static final String EXECUTE_CASHOUT = "EXECUTE_CASHOUT";
    /** Crédit destination cash-in (produit : {@code CASH_IN}) — rail SpecDriven {@code TRANSFERT}. */
    public static final String EXECUTE_CASHIN = "EXECUTE_CASHIN";

    /** Création session paiement / redirect (produit : {@code CHECKOUT}). */
    public static final String CREATE_PAYMENT = "CREATE_PAYMENT";
    /** Évaluation circuit validation (DMN / ApprovalEngine). */
    public static final String EVALUATE_APPROVAL = "EVALUATE_APPROVAL";
    public static final String GENERATE_OTP = "GENERATE_OTP";
    /** Confirmation paiement / top-up wallet (après OTP). */
    public static final String CONFIRM_PAYMENT = "CONFIRM_PAYMENT";
    /** Rattachement compte wallet (produit : {@code LINK} / alias REGISTER). */
    public static final String EXECUTE_LINK = "EXECUTE_LINK";
    /** Déliaison compte (produit futur {@code UNLINK}). */
    public static final String EXECUTE_UNLINK = "EXECUTE_UNLINK";
    /**
     * Cotation remittance (FX + frais + TTL) — config catalogue / contexte uniquement.
     * Produit associé : {@code REMITTANCE} (pas un code capability).
     */
    public static final String GET_QUOTE = "GET_QUOTE";
    /** Exécution envoi remittance (≠ {@code EXECUTE_TRANSFER} / SWIFT). */
    public static final String EXECUTE_REMITTANCE = "EXECUTE_REMITTANCE";
    /** Lookup envoi remittance par référence (produit {@code REMITTANCE_RECEIVE}). */
    public static final String LOOKUP_REMITTANCE = "LOOKUP_REMITTANCE";
    /** Contrôles locaux avant remise (ID bénéficiaire, confirm…). */
    public static final String VALIDATE_PAYOUT = "VALIDATE_PAYOUT";
    /** Exécution payout / remise chez provider remittance. */
    public static final String EXECUTE_PAYOUT = "EXECUTE_PAYOUT";
    /** Clôture receive + parent send (PAID_OUT / CREDITED). */
    public static final String COMPLETE_PAYOUT = "COMPLETE_PAYOUT";

    private CapabilityCodes() {}
}
