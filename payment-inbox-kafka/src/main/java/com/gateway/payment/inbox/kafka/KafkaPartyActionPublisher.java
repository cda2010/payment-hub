package com.gateway.payment.inbox.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Producteur Kafka. La clé est la party, pour garder l'ordre des actions d'une personne. */
public final class KafkaPartyActionPublisher implements PartyActionPublisher, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaPartyActionPublisher.class);

    private final InboxKafkaProperties properties;
    private final PartyActionCodec codec;
    private volatile KafkaProducer<String, String> producer;

    public KafkaPartyActionPublisher(InboxKafkaProperties properties, PartyActionCodec codec) {
        this.properties = properties;
        this.codec = codec;
    }

    @Override
    public void publish(PartyActionEvent event) {
        if (event == null || event.partyId() == null || event.actionType() == null || event.actionType().isBlank()) {
            throw new IllegalArgumentException("partyId et actionType requis");
        }
        String json = codec.write(event);
        try {
            producer().send(new ProducerRecord<>(properties.topic(), event.partyId().toString(), json))
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Échec d'écriture Kafka " + event.eventType() + ": " + e.getMessage(), e);
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
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.ACKS_CONFIG, "all");
                props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
                producer = new KafkaProducer<>(props);
                log.info("Inbox Kafka → {} topic={}", properties.bootstrapServers(), properties.topic());
            }
            return producer;
        }
    }

    @Override
    public void destroy() {
        KafkaProducer<String, String> current = producer;
        if (current != null) {
            current.close();
        }
    }
}
