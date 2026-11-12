package com.gateway.payment.events;

import com.gateway.payment.events.domain.DomainOutbox;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Publication optionnelle outbox → Kafka (multi-consommateurs).
 * Activé via {@code gateway.events.kafka-enabled=true}.
 */
@Component
public class OutboxKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxKafkaPublisher.class);

    private final GatewayEventsProperties properties;
    private volatile KafkaProducer<String, String> producer;

    public OutboxKafkaPublisher(GatewayEventsProperties properties) {
        this.properties = properties;
    }

    public boolean enabled() {
        return properties.kafkaEnabled();
    }

    public void publish(DomainOutbox row) {
        if (!enabled() || row == null) {
            return;
        }
        try {
            KafkaProducer<String, String> p = producer();
            String key = row.getAggregateType() + ":" + row.getAggregateId();
            String value = "{\"eventId\":\"" + row.getEventId()
                    + "\",\"eventType\":\"" + row.getEventType()
                    + "\",\"aggregateType\":\"" + row.getAggregateType()
                    + "\",\"aggregateId\":\"" + row.getAggregateId()
                    + "\",\"tenantId\":" + jsonStr(row.getTenantId())
                    + ",\"payload\":" + (row.getPayloadJson() != null ? row.getPayloadJson() : "{}")
                    + "}";
            p.send(new ProducerRecord<>(properties.kafkaTopic(), key, value)).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Échec publish Kafka outbox: " + e.getMessage(), e);
        }
    }

    private KafkaProducer<String, String> producer() {
        KafkaProducer<String, String> existing = producer;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (producer == null) {
                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers());
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.ACKS_CONFIG, "1");
                props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
                producer = new KafkaProducer<>(props);
                log.info("Outbox Kafka producer → {} topic={}",
                        properties.kafkaBootstrapServers(), properties.kafkaTopic());
            }
            return producer;
        }
    }

    @PreDestroy
    void close() {
        KafkaProducer<String, String> p = producer;
        if (p != null) {
            p.close();
        }
    }

    private static String jsonStr(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }
}
