package com.gateway.payment.pricing.tarif.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TarifConfig {

    /** Date de calcul par défaut des frais (UTC, fuseau de l'UEMOA). */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
