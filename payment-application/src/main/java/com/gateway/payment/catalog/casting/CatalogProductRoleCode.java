package com.gateway.payment.catalog.casting;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Vocabulaire plateforme des rôles produit (casting catalogue).
 * <p>
 * Les versions déclarent un sous-ensemble de ces codes dans {@code configuration.roles[]} ;
 * pas de saisie libre hors référentiel.
 */
public enum CatalogProductRoleCode {
    SOURCE("Compte émetteur"),
    DESTINATION("Compte destinataire"),
    PAYER("Payeur"),
    BILLER("Facturier"),
    BENEFICIARY("Bénéficiaire"),
    CORRESPONDENT("Correspondant"),
    /** Opérateur remittance (RIA, Western Union, …). */
    PROVIDER("Opérateur / fournisseur métier"),
    /** Réseau agent cash-in / cash-out (Wave, OM, …). */
    AGENT_NETWORK("Réseau agent");

    private final String defaultLabel;

    CatalogProductRoleCode(String defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

    public String defaultLabel() {
        return defaultLabel;
    }

    public static Optional<CatalogProductRoleCode> tryParse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String code = raw.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(v -> v.name().equals(code))
                .findFirst();
    }

    public static boolean isKnown(String raw) {
        return tryParse(raw).isPresent();
    }

    public static CatalogProductRoleCode require(String raw) {
        return tryParse(raw).orElseThrow(() -> new IllegalArgumentException("Rôle inconnu: " + raw));
    }
}
