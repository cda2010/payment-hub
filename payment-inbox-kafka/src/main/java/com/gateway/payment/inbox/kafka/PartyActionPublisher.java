package com.gateway.payment.inbox.kafka;

import java.time.Instant;
import java.util.UUID;

/**
 * Écriture Kafka d'une action « À faire ». Ne touche pas à la table {@code party_action}.
 */
public interface PartyActionPublisher {

    void publish(PartyActionEvent event);

    default void open(
            UUID partyId,
            String actionType,
            String aggregateType,
            String aggregateId,
            String subtitle,
            Instant expiresAt
    ) {
        publish(PartyActionEvent.open(partyId, null, actionType, aggregateType, aggregateId, subtitle, expiresAt));
    }

    default void complete(UUID partyId, String actionType, String aggregateType, String aggregateId) {
        publish(PartyActionEvent.complete(partyId, null, actionType, aggregateType, aggregateId, "DONE"));
    }
}
