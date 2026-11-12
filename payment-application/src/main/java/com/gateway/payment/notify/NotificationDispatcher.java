package com.gateway.payment.notify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.events.DomainEventTypes;
import com.gateway.payment.events.domain.DomainOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final List<NotificationChannel> channels;
    private final ObjectMapper objectMapper;

    public NotificationDispatcher(List<NotificationChannel> channels, ObjectMapper objectMapper) {
        this.channels = channels;
        this.objectMapper = objectMapper;
    }

    public boolean handles(DomainOutbox row) {
        return row != null && DomainEventTypes.NOTIFICATION_REQUESTED.equals(row.getEventType());
    }

    public void dispatch(DomainOutbox row) {
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(row.getPayloadJson(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Payload notification illisible", e);
        }
        String hint = payload.get("channelHint") != null
                ? String.valueOf(payload.get("channelHint"))
                : "SMS";
        boolean any = false;
        for (NotificationChannel ch : channels) {
            if (ch.supports(hint)) {
                ch.send(payload);
                any = true;
            }
        }
        if (!any) {
            log.debug("Aucun canal pour hint={} event={}", hint, row.getEventId());
        }
    }
}
