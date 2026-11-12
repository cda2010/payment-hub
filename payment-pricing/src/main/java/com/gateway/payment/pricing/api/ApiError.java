package com.gateway.payment.pricing.api;

import com.gateway.payment.pricing.tarif.validation.ValidationIssue;

import java.time.Instant;
import java.util.List;

/**
 * Même forme que l'{@code ApiError} de payment-2, pour que les clients traitent les erreurs à l'identique.
 * {@code details} liste les erreurs de configuration localisées (tranche, champ) ; absent sinon.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<ValidationIssue> details
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, null);
    }

    public static ApiError of(int status, String code, String message, String path, List<ValidationIssue> details) {
        return new ApiError(Instant.now(), status, code, message, path,
                details == null || details.isEmpty() ? null : List.copyOf(details));
    }
}
