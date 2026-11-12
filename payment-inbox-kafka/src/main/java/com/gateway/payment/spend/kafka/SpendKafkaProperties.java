package com.gateway.payment.spend.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code spend.kafka.*}. {@code enabled} publie, {@code consumer-enabled} écoute. */
@ConfigurationProperties(prefix = "spend.kafka")
public record SpendKafkaProperties(
        boolean enabled,
        boolean consumerEnabled,
        String bootstrapServers,
        String topic,
        String consumerGroup
) {
    public SpendKafkaProperties {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "localhost:9092";
        }
        if (topic == null || topic.isBlank()) {
            topic = SpendEvent.TOPIC;
        }
        if (consumerGroup == null || consumerGroup.isBlank()) {
            consumerGroup = "payment-divers";
        }
    }
}
