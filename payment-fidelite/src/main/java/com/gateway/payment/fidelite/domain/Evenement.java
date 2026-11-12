package com.gateway.payment.fidelite.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** Événement métier reçu d'un système source. Le tenant vient du jeton, jamais de ce corps. */
public record Evenement(
        UUID eventId,
        String eventType,
        String customerId,
        BigDecimal amount,
        String currency,
        Map<String, Object> attributes
) {
}
