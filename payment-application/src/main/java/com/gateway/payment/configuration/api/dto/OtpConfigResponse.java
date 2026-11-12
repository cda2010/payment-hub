package com.gateway.payment.configuration.api.dto;

import java.util.UUID;

public record OtpConfigResponse(
        UUID id,
        UUID providerId,
        boolean otpRequired,
        Integer length,
        Integer validitySeconds,
        Integer maxAttempts,
        boolean resendAllowed,
        Integer resendDelaySeconds,
        String generateOperation,
        String validateOperation
) {}
