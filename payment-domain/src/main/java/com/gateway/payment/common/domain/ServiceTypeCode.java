package com.gateway.payment.common.domain;

/**
 * Type de service produit (aligné référentiel {@code service_type} / Omnicanal).
 * <p>
 * Un produit catalogue déclare son type dans {@code configuration.serviceType}.
 * S'il est absent ou inconnu, le runtime utilise {@link #DEFAUT}.
 */
public enum ServiceTypeCode {
    LINK,
    UNLINK,
    CONSULTATION_SOLDE,
    TRANSFERT,
    PAYEMENT,
    CONSULTATIONHIST,
    INFOCLIENT,
    /** Paiement de facture (ex-BILL_PAYMENT). */
    PAIEMENT_FACTURE,
    /**
     * Type générique sélectionnable quand aucun type métier plus précis ne s'applique
     * (nouveaux produits catalogue, parcours hors transfert / facture / link).
     */
    DEFAUT;

    /** Repli catalogue / runtime quand {@code configuration.serviceType} est absent. */
    public static final ServiceTypeCode CATALOG_DEFAULT = DEFAUT;

    public boolean requiresPositiveAmount() {
        return this == TRANSFERT || this == PAYEMENT || this == PAIEMENT_FACTURE;
    }

    public boolean isTransfer() {
        return this == TRANSFERT;
    }

    public boolean isBillPayment() {
        return this == PAIEMENT_FACTURE;
    }

    public boolean isDefault() {
        return this == DEFAUT;
    }

    /**
     * Code flow historique (rétrocompat). La résolution runtime utilise
     * {@code PaymentFlow.serviceType}, pas ce code.
     */
    public String preferredFlowCode() {
        return name();
    }

    /**
     * Clé BPMN par défaut quand aucun flow provider-spécifique n'est encore
     * renseigné.
     */
    public String defaultBpmProcessKey() {
        return switch (this) {
            case TRANSFERT -> "providerWalletTransfer";
            case PAIEMENT_FACTURE -> "providerBillPay";
            case CONSULTATION_SOLDE, CONSULTATIONHIST -> "providerBalanceInquiry";
            case INFOCLIENT, PAYEMENT, DEFAUT -> "providerOtpCheckout";
            case LINK -> "providerAccountLink";
            case UNLINK -> "providerAccountUnlink";
        };
    }

    public static ServiceTypeCode fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        return switch (n) {
            case "CONSULTATIONSOLDE", "SOLDE", "BALANCE" -> CONSULTATION_SOLDE;
            case "TRANSFER", "TRANSFERT" -> TRANSFERT;
            case "PAYMENT", "PAYEMENT", "PAIEMENT", "CHECKOUT" -> PAYEMENT;
            case "PAIEMENT_FACTURE", "BILL_PAYMENT", "BILL", "BILLPAY" -> PAIEMENT_FACTURE;
            case "LINK" -> LINK;
            case "UNLINK" -> UNLINK;
            case "CONSULTATION_SOLDE" -> CONSULTATION_SOLDE;
            case "DEFAULT", "DEFAUT", "GENERIC", "GENERIQUE", "OTHER", "AUTRE" -> DEFAUT;
            default -> {
                try {
                    yield ServiceTypeCode.valueOf(n);
                } catch (IllegalArgumentException ex) {
                    yield null;
                }
            }
        };
    }
}
