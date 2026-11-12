package com.gateway.payment.inbox.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Kafka éteint : l'appel est ignoré, le service appelant continue. */
public final class NoOpPartyActionPublisher implements PartyActionPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpPartyActionPublisher.class);

    @Override
    public void publish(PartyActionEvent event) {
        if (event == null) {
            return;
        }
        log.debug("inbox.kafka.enabled=false, message {} ignoré pour {}", event.eventType(), event.partyId());
    }
}
