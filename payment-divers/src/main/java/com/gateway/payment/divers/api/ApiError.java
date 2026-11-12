package com.gateway.payment.divers.api;

import java.time.Instant;

/** Même forme que l'{@code ApiError} des autres services, pour que les clients traitent les erreurs à l'identique. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path);
    }
}
