package com.gateway.payment.network.api;

import java.time.Instant;
import java.util.List;

/**
 * Même forme que l'{@code ApiError} de payment-2, pour que les clients traitent les erreurs à l'identique.
 * {@code details} liste les champs en erreur d'une requête invalide ; absent sinon.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<Detail> details
) {
    public record Detail(String code, String champ, String message) {
    }

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, null);
    }

    public static ApiError of(int status, String code, String message, String path, List<Detail> details) {
        return new ApiError(Instant.now(), status, code, message, path,
                details == null || details.isEmpty() ? null : List.copyOf(details));
    }
}
