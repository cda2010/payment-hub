package com.gateway.payment.security.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        boolean locked,
        List<String> roles,
        Instant createdAt,
        Instant lastLoginAt
) {}
