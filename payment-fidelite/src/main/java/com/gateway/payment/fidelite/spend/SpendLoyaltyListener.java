package com.gateway.payment.fidelite.spend;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.service.AttributionService;
import com.gateway.payment.spend.kafka.SpendCodec;
import com.gateway.payment.spend.kafka.SpendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Un paiement réussi sur {@code payment.spend} alimente les règles publiées. */
@Component
@ConditionalOnProperty(prefix = "spend.kafka", name = "consumer-enabled", havingValue = "true")
public class SpendLoyaltyListener {

    private static final Logger log = LoggerFactory.getLogger(SpendLoyaltyListener.class);

    private final AttributionService attribution;
    private final SpendCodec codec;

    public SpendLoyaltyListener(AttributionService attribution, SpendCodec codec) {
        this.attribution = attribution;
        this.codec = codec;
    }

    @KafkaListener(topics = "${spend.kafka.topic:payment.spend}", containerFactory = "spendLoyaltyKafkaFactory")
    public void onMessage(String json) {
        SpendEvent spend;
        try {
            spend = codec.read(json);
        } catch (Exception e) {
            log.warn("Message payment.spend illisible, ignoré");
            return;
        }
        Evenement event = SpendLoyaltyMapper.toEvenement(spend);
        if (event == null) {
            log.warn("Message payment.spend sans paiement ou sans client, ignoré");
            return;
        }
        try {
            attribution.recevoir(SpendLoyaltyMapper.tenant(spend), event);
        } catch (IllegalArgumentException e) {
            log.warn("Paiement {} non attribué : {}", spend.paymentId(), e.getMessage());
        }
    }
}
