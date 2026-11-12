package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.exception.BusinessException;

import java.util.Locale;

/**
 * Environnements standard d'un fournisseur.
 */
public enum ProviderEnvironmentCode {
    SANDBOX,
    DEVELOPPEMENT,
    PRODUCTION;

    public static ProviderEnvironmentCode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("ENVIRONMENT_CODE_INVALID", "Code environnement requis");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SANDBOX" -> SANDBOX;
            case "DEVELOPPEMENT", "DEVELOPMENT", "DEV" -> DEVELOPPEMENT;
            case "PRODUCTION", "PROD" -> PRODUCTION;
            default -> throw new BusinessException(
                    "ENVIRONMENT_CODE_INVALID",
                    "Code environnement invalide (SANDBOX, DEVELOPPEMENT, PRODUCTION)");
        };
    }
}
