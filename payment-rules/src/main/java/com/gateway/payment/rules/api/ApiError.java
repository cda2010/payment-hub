package com.gateway.payment.rules.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, null);
    }

    public static ApiError of(int status, String code, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, code, message, path,
                details == null || details.isEmpty() ? null : List.copyOf(details));
    }
}
