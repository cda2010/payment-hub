package com.gateway.payment.rules.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryJob.class);
    private final CumulService cumulService;

    public ReservationExpiryJob(CumulService cumulService) {
        this.cumulService = cumulService;
    }

    @Scheduled(fixedDelayString = "${rules.reservation.expiry-delay-ms:30000}")
    public void expire() {
        int n = cumulService.expireHeld();
        if (n > 0) {
            log.info("Réservations expirées libérées: {}", n);
        }
    }
}
