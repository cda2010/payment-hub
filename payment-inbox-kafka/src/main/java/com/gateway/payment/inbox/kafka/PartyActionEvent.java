package com.gateway.payment.inbox.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Fait publié sur Kafka. Le titre, l'icône et la route restent dans le catalogue {@code action_type}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PartyActionEvent(
        String eventType,
        UUID partyId,
        String tenantId,
        String actionType,
        String subtitle,
        String aggregateType,
        String aggregateId,
        Instant expiresAt,
        String status
) {
    public static final String OPEN = "party.action.open";
    public static final String COMPLETE = "party.action.complete";
    public static final String TOPIC = "party.actions";

    public static PartyActionEvent open(
            UUID partyId,
            String tenantId,
            String actionType,
            String aggregateType,
            String aggregateId,
            String subtitle,
            Instant expiresAt
    ) {
        return new PartyActionEvent(
                OPEN, partyId, tenantId, actionType, subtitle, aggregateType, aggregateId, expiresAt, null);
    }

    public static PartyActionEvent complete(
            UUID partyId,
            String tenantId,
            String actionType,
            String aggregateType,
            String aggregateId,
            String status
    ) {
        return new PartyActionEvent(
                COMPLETE, partyId, tenantId, actionType, null, aggregateType, aggregateId, null,
                status == null || status.isBlank() ? "DONE" : status);
    }

    public boolean opening() {
        return OPEN.equals(eventType);
    }
}
