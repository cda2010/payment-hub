package com.gateway.payment.events;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.events")
public record GatewayEventsProperties(
        boolean outboxEnabled,
        long pollDelayMs,
        int batchSize,
        int maxAttempts,
        boolean outboundWebhooksEnabled,
        boolean inboundRequireAuth,
        boolean kafkaEnabled,
        String kafkaTopic,
        String kafkaBootstrapServers
) {
    public GatewayEventsProperties {
        if (pollDelayMs <= 0) {
            pollDelayMs = 5000;
        }
        if (batchSize <= 0) {
            batchSize = 50;
        }
        if (maxAttempts <= 0) {
            maxAttempts = 8;
        }
        if (kafkaTopic == null || kafkaTopic.isBlank()) {
            kafkaTopic = "payment.domain-events";
        }
        if (kafkaBootstrapServers == null || kafkaBootstrapServers.isBlank()) {
            kafkaBootstrapServers = "localhost:9092";
        }
    }
}
