package com.gateway.payment.omnicanal.domain;

/**
 * Cas d'exécution d'une jambe FSE (miroir Omnicanal {@code CasExecution}).
 * {@link #REVERSION} inverse les rôles compte source/dest à la compilation.
 */
public enum CasExecution {
    NORMAL,
    REVERSION;

    public static CasExecution fromParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return NORMAL;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }

    public boolean isReversion() {
        return this == REVERSION;
    }
}
