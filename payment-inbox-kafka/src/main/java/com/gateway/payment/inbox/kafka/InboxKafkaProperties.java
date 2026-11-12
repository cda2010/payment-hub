package com.gateway.payment.inbox.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code inbox.kafka.*}. {@code enabled} publie, {@code consumer-enabled} écoute.
 * Les deux sont éteints tant qu'un broker n'est pas là.
 */
@ConfigurationProperties(prefix = "inbox.kafka")
public record InboxKafkaProperties(
        boolean enabled,
        boolean consumerEnabled,
        String bootstrapServers,
        String topic,
        String consumerGroup
) {
    public InboxKafkaProperties {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "localhost:9092";
        }
        if (topic == null || topic.isBlank()) {
            topic = PartyActionEvent.TOPIC;
        }
        if (consumerGroup == null || consumerGroup.isBlank()) {
            consumerGroup = "payment-identity";
        }
    }
}
