package com.gateway.payment.spend.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Paiement réussi publié par la passerelle. payment-divers décide s'il compte
 * comme une dépense et dans quelle {@code categorie_depense}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpendEvent(
        String paymentId,
        UUID partyId,
        String tenantId,
        BigDecimal amount,
        String currency,
        String serviceType,
        String productCode,
        String categorieHint,
        String merchantName,
        Instant occurredAt,
        String channel,
        String destinationUniteId,
        String destinationUniteType,
        List<UniteRef> lignee
) {
    public static final String TOPIC = "payment.spend";
}
