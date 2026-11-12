package com.gateway.payment.spend.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NoOpSpendPublisher implements SpendPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpSpendPublisher.class);

    @Override
    public void publish(SpendEvent event) {
        if (event == null) {
            return;
        }
        log.debug("spend.kafka.enabled=false, paiement {} ignoré", event.paymentId());
    }
}
