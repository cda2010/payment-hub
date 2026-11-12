package com.gateway.payment.payment.api.dto;

import java.time.Instant;

public record PaymentEventDto(
        Instant timestamp,
        String eventType,
        String message
) {}
