package com.gateway.payment.common.domain;

import java.util.Locale;

/**
 * Canal d'accès client — distinct du « canal » Omnicanal / réseau agent
 * et distinct de {@link ProductAudience} (segment client).
 */
public enum AccessChannel {
    MOBILE,
    /** Portail / back-office web client (hors app native). */
    WEB,
    GAB,
    USSD,
    BACKOFFICE;

    public static AccessChannel parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return AccessChannel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static AccessChannel require(String raw) {
        AccessChannel channel = parse(raw);
        if (channel == null) {
            throw new IllegalArgumentException("Canal d'accès invalide: " + raw
                    + " (attendu: MOBILE, WEB, GAB, USSD, BACKOFFICE)");
        }
        return channel;
    }
}
