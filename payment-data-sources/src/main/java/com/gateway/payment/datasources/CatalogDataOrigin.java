package com.gateway.payment.datasources;

import java.util.Locale;

/**
 * Origine d'hydratation d'une section présentation / {@code rfDataSource}.
 * <ul>
 *   <li>{@link #LOCAL} — services payment-2 (biller, static items, …)</li>
 *   <li>{@link #OMNICANAL} — jambe FSE {@code typeMethode=DATASOURCE}</li>
 *   <li>{@link #GATEWAY} — Product Gateway d'une app peer ({@code payment-data-sources})</li>
 * </ul>
 */
public enum CatalogDataOrigin {
    LOCAL,
    OMNICANAL,
    /** Alias YAML / Formio : {@code REMOTE_APP} → {@link #GATEWAY}. */
    GATEWAY;

    public static CatalogDataOrigin parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return LOCAL;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if ("REMOTE_APP".equals(n) || "REMOTE".equals(n) || "PEER".equals(n)) {
            return GATEWAY;
        }
        for (CatalogDataOrigin o : values()) {
            if (o.name().equals(n)) {
                return o;
            }
        }
        return LOCAL;
    }
}
