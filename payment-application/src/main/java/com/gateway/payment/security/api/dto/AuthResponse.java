package com.gateway.payment.security.api.dto;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String username,
            String email,
            String fullName,
            List<String> roles
    ) {}
}
