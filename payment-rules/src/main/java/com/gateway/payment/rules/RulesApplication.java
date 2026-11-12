package com.gateway.payment.rules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Moteur de règles métier / réglementaires — application autonome, base dédiée.
 * Contrat HTTP : {@code /api/v1/rules/**}, {@code /api/v1/rule-groups/**}, {@code /api/v1/limits/**}.
 * Distinct de {@code payment-soft-rules} (contrôles catalogue BPMN).
 */
@SpringBootApplication
@EnableScheduling
public class RulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RulesApplication.class, args);
    }
}
