package com.gateway.payment.catalog.capability;

import java.util.Map;

/**
 * Brique fonctionnelle réutilisable — un produit catalogue compose des capabilities,
 * il n'ajoute pas de service Java dédié.
 */
public interface CapabilityHandler {

    /** Code stable (ex. {@code CALCULATE_FEES}, {@code NOTIFY}). */
    String code();

    /** Opération fournisseur appelée par défaut ; {@code null} pour une capability locale. */
    default String defaultOperation() {
        return null;
    }

    CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration);
}
