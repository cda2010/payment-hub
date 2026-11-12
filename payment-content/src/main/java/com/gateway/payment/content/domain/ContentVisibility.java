package com.gateway.payment.content.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Normalisation / matching des listes de visibilité.
 * Valeurs alignées sur {@code AccessChannel} et {@code ProductAudience} (payment-domain).
 */
public final class ContentVisibility {

    public static final Set<String> CHANNELS = Set.of(
            "MOBILE", "WEB", "GAB", "USSD", "BACKOFFICE"
    );

    public static final Set<String> AUDIENCES = Set.of(
            "RETAIL", "CORPORATE", "AGENT", "MERCHANT", "BACKOFFICE"
    );

    private ContentVisibility() {
    }

    /** Liste vide ou null = pas de restriction (visible partout). */
    public static boolean matches(List<String> allowed, String requested) {
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        if (requested == null || requested.isBlank()) {
            return true;
        }
        String want = requested.trim().toUpperCase(Locale.ROOT);
        return allowed.stream().anyMatch(a -> want.equalsIgnoreCase(a));
    }

    public static List<String> normalizeChannels(List<String> raw) {
        return normalize(raw, CHANNELS);
    }

    public static List<String> normalizeAudiences(List<String> raw) {
        return normalize(raw, AUDIENCES);
    }

    private static List<String> normalize(List<String> raw, Set<String> allowed) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String item : raw) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String code = item.trim().toUpperCase(Locale.ROOT);
            if (allowed.contains(code)) {
                out.add(code);
            }
        }
        return new ArrayList<>(out);
    }
}
