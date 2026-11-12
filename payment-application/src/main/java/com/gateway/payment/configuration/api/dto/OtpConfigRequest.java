package com.gateway.payment.configuration.api.dto;

import jakarta.validation.constraints.Size;

public record OtpConfigRequest(
        boolean otpRequired,
        Integer length,
        Integer validitySeconds,
        Integer maxAttempts,
        boolean resendAllowed,
        Integer resendDelaySeconds,
        @Size(max = 50) String generateOperation,
        @Size(max = 50) String validateOperation
) {}
