package com.gateway.payment.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.events.domain.DomainOutbox;
import com.gateway.payment.events.domain.OutboxStatus;
import com.gateway.payment.events.repository.DomainOutboxRepository;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Écrit dans {@code domain_outbox} dans la même transaction métier.
 * Le worker {@link OutboxDispatcherJob} publie ensuite (webhooks sortants, etc.).
 */
@Service
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final DomainOutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final GatewayEventsProperties properties;

    public DomainEventPublisher(
            DomainOutboxRepository outbox,
            ObjectMapper objectMapper,
            GatewayEventsProperties properties
    ) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public void paymentTerminal(PaymentTransaction tx, String eventType) {
        if (tx == null || eventType == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentId", tx.getPaymentId());
        payload.put("tenantId", tx.getTenantId());
        payload.put("providerCode", tx.getProviderCode());
        payload.put("serviceType", tx.getServiceType() != null ? tx.getServiceType().name() : null);
        payload.put("status", tx.getStatus() != null ? tx.getStatus().name() : null);
        payload.put("amount", tx.getAmount());
        payload.put("currency", tx.getCurrency());
        payload.put("customerReference", tx.getCustomerReference());
        payload.put("providerReference", tx.getProviderReference());
        payload.put("occurredAt", Instant.now().toString());
        publish(eventType, "payment", tx.getPaymentId(), tx.getTenantId(), payload);
    }

    /**
     * Publie {@link DomainEventTypes#PARTY_ACTION_OPEN}. Le dispatcher rappelle
     * payment-identity, qui porte l'inbox.
     */
    @Transactional
    public void partyActionOpen(Map<String, Object> payload) {
        if (payload == null || payload.get("partyId") == null) {
            return;
        }
        String partyId = String.valueOf(payload.get("partyId"));
        publish(DomainEventTypes.PARTY_ACTION_OPEN, "party", partyId,
                payload.get("tenantId") != null ? String.valueOf(payload.get("tenantId")) : null,
                payload);
    }

    @Transactional
    public void partyActionComplete(Map<String, Object> payload) {
        if (payload == null || payload.get("partyId") == null) {
            return;
        }
        String partyId = String.valueOf(payload.get("partyId"));
        publish(DomainEventTypes.PARTY_ACTION_COMPLETE, "party", partyId,
                payload.get("tenantId") != null ? String.valueOf(payload.get("tenantId")) : null,
                payload);
    }

    @Transactional
    public void notificationRequested(PaymentTransaction tx, String channelHint) {
        notificationRequested(tx, channelHint, null);
    }

    /**
     * @param messageExtras champs optionnels (body, title, notificationCode…) issus du catalogue produit
     */
    @Transactional
    public void notificationRequested(
            PaymentTransaction tx,
            String channelHint,
            Map<String, Object> messageExtras
    ) {
        if (tx == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentId", tx.getPaymentId());
        payload.put("tenantId", tx.getTenantId());
        payload.put("channelHint", channelHint);
        payload.put("phoneNumber", tx.getPhoneNumber());
        payload.put("customerReference", tx.getCustomerReference());
        payload.put("status", tx.getStatus() != null ? tx.getStatus().name() : null);
        payload.put("occurredAt", Instant.now().toString());
        if (messageExtras != null) {
            messageExtras.forEach((k, v) -> {
                if (k != null && !k.isBlank() && v != null) {
                    payload.put(k, v);
                }
            });
        }
        publish(DomainEventTypes.NOTIFICATION_REQUESTED, "payment", tx.getPaymentId(), tx.getTenantId(), payload);
    }

    @Transactional
    public void publish(
            String eventType,
            String aggregateType,
            String aggregateId,
            String tenantId,
            Map<String, Object> payload
    ) {
        if (!properties.outboxEnabled()) {
            log.debug("Outbox désactivé — event {} ignoré", eventType);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
            DomainOutbox row = DomainOutbox.builder()
                    .eventId("EVT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase())
                    .eventType(eventType)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .tenantId(tenantId)
                    .payloadJson(json)
                    .status(OutboxStatus.PENDING)
                    .attempts(0)
                    .nextAttemptAt(Instant.now())
                    .build();
            outbox.save(row);
        } catch (Exception e) {
            log.error("Échec écriture outbox {}: {}", eventType, e.getMessage());
            throw new IllegalStateException("Impossible d'enregistrer l'événement " + eventType, e);
        }
    }
}
