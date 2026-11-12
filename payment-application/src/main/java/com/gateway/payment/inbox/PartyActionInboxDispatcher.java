package com.gateway.payment.inbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.events.DomainEventTypes;
import com.gateway.payment.inbox.kafka.PartyActionEvent;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import com.gateway.payment.events.domain.DomainOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Rejoue un événement d'outbox déjà enregistré en le publiant sur Kafka.
 */
@Component
public class PartyActionInboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PartyActionInboxDispatcher.class);

    private final PartyActionPublisher actions;
    private final ObjectMapper objectMapper;

    public PartyActionInboxDispatcher(PartyActionPublisher actions, ObjectMapper objectMapper) {
        this.actions = actions;
        this.objectMapper = objectMapper;
    }

    public boolean handles(DomainOutbox row) {
        if (row == null || row.getEventType() == null) {
            return false;
        }
        String t = row.getEventType();
        return DomainEventTypes.PARTY_ACTION_OPEN.equals(t)
                || DomainEventTypes.PARTY_ACTION_COMPLETE.equals(t);
    }

    public void dispatch(DomainOutbox row) {
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(row.getPayloadJson(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Payload party.action illisible", e);
        }
        if (DomainEventTypes.PARTY_ACTION_OPEN.equals(row.getEventType())) {
            open(row, payload);
        } else {
            complete(payload);
        }
    }

    private void open(DomainOutbox row, Map<String, Object> payload) {
        UUID partyId = uuid(payload.get("partyId"));
        if (partyId == null) {
            log.warn("party.action.open sans partyId event={}", row.getEventId());
            return;
        }
        String actionType = str(payload.get("actionType"));
        if (!StringUtils.hasText(actionType)) {
            log.warn("party.action.open incomplet event={}", row.getEventId());
            return;
        }
        Instant expiresAt = null;
        String rawExp = str(payload.get("expiresAt"));
        if (StringUtils.hasText(rawExp)) {
            try {
                expiresAt = Instant.parse(rawExp);
            } catch (Exception ignored) {
                // ignore
            }
        }
        actions.publish(PartyActionEvent.open(
                partyId,
                str(payload.get("tenantId")),
                actionType,
                str(payload.get("aggregateType")),
                str(payload.get("aggregateId")),
                str(payload.get("subtitle")),
                expiresAt
        ));
    }

    private void complete(Map<String, Object> payload) {
        UUID partyId = uuid(payload.get("partyId"));
        String actionType = str(payload.get("actionType"));
        if (partyId == null || !StringUtils.hasText(actionType)) {
            return;
        }
        String status = str(payload.get("status"));
        actions.publish(PartyActionEvent.complete(
                partyId,
                str(payload.get("tenantId")),
                actionType,
                str(payload.get("aggregateType")),
                str(payload.get("aggregateId")),
                StringUtils.hasText(status) ? status : "DONE"
        ));
    }

    private static UUID uuid(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(o));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
