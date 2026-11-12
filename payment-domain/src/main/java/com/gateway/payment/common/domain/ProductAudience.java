package com.gateway.payment.common.domain;

import java.util.Locale;

/**
 * Audience cible d'un produit catalogue — orthogonal au {@link AccessChannel}
 * (canal d'accès MOBILE/WEB/…).
 *
 * <p>Déclaré sur {@code configuration.supportedAudiences} / {@code defaultAudience}.
 */
public enum ProductAudience {
    /** Particuliers (app mobile, web particulier). */
    RETAIL,
    /** Entreprises / portail corporate. */
    CORPORATE,
    /** Réseau d'agents. */
    AGENT,
    /** Marchands / till / encaissement. */
    MERCHANT,
    /** Portail / outils internes banque. */
    BACKOFFICE;

    public static ProductAudience parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT);
        // Alias FR / legacy
        if ("MARCHAND".equals(n) || "MERCHANTS".equals(n)) {
            return MERCHANT;
        }
        if ("PARTICULIER".equals(n) || "PERSONAL".equals(n) || "B2C".equals(n)) {
            return RETAIL;
        }
        if ("ENTREPRISE".equals(n) || "B2B".equals(n) || "BUSINESS".equals(n)) {
            return CORPORATE;
        }
        try {
            return ProductAudience.valueOf(n);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static ProductAudience require(String raw) {
        ProductAudience audience = parse(raw);
        if (audience == null) {
            throw new IllegalArgumentException(
                    "Audience invalide: " + raw
                            + " (attendu: RETAIL, CORPORATE, AGENT, MERCHANT, BACKOFFICE)");
        }
        return audience;
    }
}
