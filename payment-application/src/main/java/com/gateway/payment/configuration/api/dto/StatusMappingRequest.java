package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * @param messages messages multi-langue (fr, en, …) — préféré
 * @param message  legacy mono-langue (converti en fr/en si messages absent)
 */
public record StatusMappingRequest(
        @NotBlank @Size(max = 100) String providerStatus,
        @NotNull PaymentStatus gatewayStatus,
        Map<String, String> messages,
        String message
) {
    public StatusMappingRequest {
        if (messages == null && message != null && !message.isBlank()) {
            messages = Map.of("fr", message.trim(), "en", message.trim());
        }
    }
}
