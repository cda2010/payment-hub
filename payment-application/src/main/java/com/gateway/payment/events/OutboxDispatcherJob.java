package com.gateway.payment.events;

import com.gateway.payment.events.domain.DomainOutbox;
import com.gateway.payment.events.domain.OutboxStatus;
import com.gateway.payment.events.repository.DomainOutboxRepository;
import com.gateway.payment.inbox.PartyActionInboxDispatcher;
import com.gateway.payment.notify.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OutboxDispatcherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcherJob.class);

    private final DomainOutboxRepository outbox;
    private final OutboundWebhookDispatcher webhooks;
    private final NotificationDispatcher notifications;
    private final PartyActionInboxDispatcher partyActions;
    private final OutboxKafkaPublisher kafka;
    private final GatewayEventsProperties properties;

    public OutboxDispatcherJob(
            DomainOutboxRepository outbox,
            OutboundWebhookDispatcher webhooks,
            NotificationDispatcher notifications,
            PartyActionInboxDispatcher partyActions,
            OutboxKafkaPublisher kafka,
            GatewayEventsProperties properties
    ) {
        this.outbox = outbox;
        this.webhooks = webhooks;
        this.notifications = notifications;
        this.partyActions = partyActions;
        this.kafka = kafka;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${gateway.events.poll-delay-ms:5000}")
    @Transactional
    public void poll() {
        if (!properties.outboxEnabled()) {
            return;
        }
        List<DomainOutbox> due = outbox.findDue(
                OutboxStatus.PENDING,
                Instant.now(),
                PageRequest.of(0, properties.batchSize()));
        for (DomainOutbox row : due) {
            try {
                if (notifications.handles(row)) {
                    notifications.dispatch(row);
                }
                if (partyActions.handles(row)) {
                    partyActions.dispatch(row);
                }
                if (kafka.enabled()) {
                    kafka.publish(row);
                }
                webhooks.dispatch(row);
                row.setStatus(OutboxStatus.SENT);
                row.setPublishedAt(Instant.now());
                row.setLastError(null);
            } catch (Exception e) {
                int attempts = row.getAttempts() + 1;
                row.setAttempts(attempts);
                row.setLastError(truncate(e.getMessage()));
                if (attempts >= properties.maxAttempts()) {
                    row.setStatus(OutboxStatus.FAILED);
                    log.warn("Outbox {} abandonné après {} tentatives: {}", row.getEventId(), attempts, e.getMessage());
                } else {
                    long backoffSec = Math.min(300, (long) Math.pow(2, Math.min(attempts, 8)));
                    row.setNextAttemptAt(Instant.now().plus(backoffSec, ChronoUnit.SECONDS));
                    log.debug("Outbox {} retry dans {}s: {}", row.getEventId(), backoffSec, e.getMessage());
                }
            }
            outbox.save(row);
        }
    }

    private static String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
