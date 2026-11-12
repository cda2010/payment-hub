package com.gateway.payment.catalog.presentation;

import java.util.Locale;

/**
 * Pattern UI catalogue — whitelist alignée mobile ({@code displayKind}).
 * Le serveur compose ; le client mappe vers un presenter.
 */
public enum DisplayKind {
    FILTER_CHIPS,
    SHORTCUT_SLIDE,
    LEAF_LIST,
    SECTIONED_LIST,
    ENTRY_GRID,
    DRILL_NAV;

    public static DisplayKind parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (DisplayKind k : values()) {
            if (k.name().equals(n)) {
                return k;
            }
        }
        return null;
    }

    /** Kind inconnu → LEAF_LIST (safe fallback client). */
    public static DisplayKind parseOrLeaf(String raw) {
        DisplayKind k = parse(raw);
        return k != null ? k : LEAF_LIST;
    }
}
