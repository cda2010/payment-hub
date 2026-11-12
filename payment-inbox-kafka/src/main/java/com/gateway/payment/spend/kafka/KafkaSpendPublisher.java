package com.gateway.payment.spend.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class KafkaSpendPublisher implements SpendPublisher, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaSpendPublisher.class);

    private final SpendKafkaProperties properties;
    private final SpendCodec codec;
    private volatile KafkaProducer<String, String> producer;

    public KafkaSpendPublisher(SpendKafkaProperties properties, SpendCodec codec) {
        this.properties = properties;
        this.codec = codec;
    }

    @Override
    public void publish(SpendEvent event) {
        if (event == null || event.paymentId() == null || event.paymentId().isBlank() || event.partyId() == null) {
            throw new IllegalArgumentException("paymentId et partyId requis");
        }
        String json = codec.write(event);
        try {
            producer().send(new ProducerRecord<>(properties.topic(), event.partyId().toString(), json))
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Échec d'écriture Kafka payment.spend: " + e.getMessage(), e);
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
                log.info("Spend Kafka → {} topic={}", properties.bootstrapServers(), properties.topic());
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
