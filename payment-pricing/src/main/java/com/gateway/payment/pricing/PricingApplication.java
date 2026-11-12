package com.gateway.payment.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Moteur Pricing (frais, taux, limites) — application autonome, base dédiée.
 * Contrat HTTP : {@code /api/v1/pricing/**}. Les tokens viennent de Keycloak (ou de l'ancien login payment-2).
 */
@SpringBootApplication
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }
}
