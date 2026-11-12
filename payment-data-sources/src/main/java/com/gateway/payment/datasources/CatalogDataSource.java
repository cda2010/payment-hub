package com.gateway.payment.datasources;

import java.util.Locale;

/**
 * Source de données d'une section catalogue (résolue côté gateway).
 */
public enum CatalogDataSource {
    /** Catégories facturiers actives. */
    BILLER_CATEGORIES,
    /** Page facturiers (filtre / q / pagination). */
    BILLERS,
    /** Favoris — items statiques section ou liste vide. */
    BILL_FAVORITES,
    /** Items déclarés dans la config section. */
    STATIC_ITEMS,
    /**
     * Participants transfert actifs, filtrés par {@code params.role}
     * (`SOURCE` → émetteurs, `DESTINATION` → destinataires, absent → tous).
     */
    TRANSFER_PARTICIPANTS,
    /**
     * Participants déclarés au casting du produit ({@code configuration.participants}),
     * filtrés par {@code params.role} (`PAYER`, `SOURCE`, `DESTINATION`…).
     * Même source d'éligibilité que le casting runtime — comme ACCOUNT_TRANSFER
     * pour les rails, appliqué ici au choix du moyen de paiement (CHECKOUT…).
     */
    CATALOG_PARTICIPANTS,
    /**
     * Carnet de bénéficiaires enregistrés ({@code transfer_beneficiary}),
     * pour préremplir un transfert (phone / compte + destinationProvider).
     */
    TRANSFER_BENEFICIARIES,
    /**
     * Banques émettrices du produit catalogue {@code SWIFT}
     * ({@code configuration.participants} filtrés {@code role=SOURCE}).
     */
    SWIFT_EMITTER_BANKS,
    /**
     * Clé libre (ex. PARTICIPANTS) — handler LOCAL dédié ou origin OMNICANAL.
     * Le code exact est conservé dans {@code DataSourceSection.dataSourceCode()}.
     */
    CUSTOM;

    public static CatalogDataSource parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (CatalogDataSource s : values()) {
            if (s.name().equals(n)) {
                return s;
            }
        }
        return CUSTOM;
    }
}
