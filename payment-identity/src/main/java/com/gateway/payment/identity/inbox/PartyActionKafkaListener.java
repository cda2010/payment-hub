package com.gateway.payment.identity.inbox;

import com.gateway.payment.identity.api.dto.PartyActionDtos.CompleteActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.OpenActionRequest;
import com.gateway.payment.identity.service.PartyActionService;
import com.gateway.payment.inbox.kafka.PartyActionCodec;
import com.gateway.payment.inbox.kafka.PartyActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Transforme un message {@code party.actions} en ligne {@code party_action}.
 * Activé avec {@code inbox.kafka.consumer-enabled=true}.
 */
@Component
@ConditionalOnProperty(prefix = "inbox.kafka", name = "consumer-enabled", havingValue = "true")
public class PartyActionKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PartyActionKafkaListener.class);

    private final PartyActionService actions;
    private final PartyActionCodec codec;

    public PartyActionKafkaListener(PartyActionService actions, PartyActionCodec codec) {
        this.actions = actions;
        this.codec = codec;
    }

    @KafkaListener(
            topics = "${inbox.kafka.topic:party.actions}",
            containerFactory = "partyActionKafkaFactory"
    )
    public void onMessage(String json) {
        PartyActionEvent event;
        try {
            event = codec.read(json);
        } catch (Exception e) {
            log.warn("Message party.action ignoré: {}", e.getMessage());
            return;
        }
        if (event.partyId() == null || event.actionType() == null) {
            log.warn("Message party.action incomplet");
            return;
        }
        try {
            if (PartyActionEvent.OPEN.equals(event.eventType())) {
                actions.open(event.partyId(), new OpenActionRequest(
                        event.tenantId(),
                        event.actionType(),
                        event.subtitle(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.expiresAt(),
                        null
                ));
                log.info("Action ouverte {} pour {}", event.actionType(), event.partyId());
            } else if (PartyActionEvent.COMPLETE.equals(event.eventType())) {
                actions.complete(event.partyId(), new CompleteActionRequest(
                        event.actionType(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.status()
                ));
                log.info("Action clôturée {} pour {}", event.actionType(), event.partyId());
            } else {
                log.warn("Événement party.action inconnu: {}", event.eventType());
            }
        } catch (ResponseStatusException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.is4xxClientError()) {
                log.warn("party.action {} {} non appliqué: {}", event.eventType(), event.actionType(), e.getReason());
                return;
            }
            throw e;
        }
    }
}
