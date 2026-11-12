package com.gateway.payment.divers.spend;

import com.gateway.payment.divers.service.DepenseService;
import com.gateway.payment.spend.kafka.SpendCodec;
import com.gateway.payment.spend.kafka.SpendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Transforme un message {@code payment.spend} en ligne {@code depense}. */
@Component
@ConditionalOnProperty(prefix = "spend.kafka", name = "consumer-enabled", havingValue = "true")
public class SpendKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(SpendKafkaListener.class);

    private final DepenseService depenses;
    private final SpendCodec codec;

    public SpendKafkaListener(DepenseService depenses, SpendCodec codec) {
        this.depenses = depenses;
        this.codec = codec;
    }

    @KafkaListener(topics = "${spend.kafka.topic:payment.spend}", containerFactory = "spendKafkaFactory")
    public void onMessage(String json) {
        SpendEvent event;
        try {
            event = codec.read(json);
        } catch (Exception e) {
            log.warn("Message payment.spend illisible, ignoré");
            return;
        }
        depenses.record(event);
    }
}
