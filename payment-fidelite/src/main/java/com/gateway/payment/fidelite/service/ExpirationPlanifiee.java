package com.gateway.payment.fidelite.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExpirationPlanifiee {

    private final ExpirationService expiration;

    public ExpirationPlanifiee(ExpirationService expiration) {
        this.expiration = expiration;
    }

    @Scheduled(cron = "0 20 2 * * *")
    public void nuit() {
        Instant maintenant = Instant.now();
        expiration.expirer(maintenant);
        expiration.prevenir(maintenant);
    }
}
