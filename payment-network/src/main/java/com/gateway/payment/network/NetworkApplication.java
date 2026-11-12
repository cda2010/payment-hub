package com.gateway.payment.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Réseau de distribution (unités d'organisation et regroupements) — application autonome, base dédiée.
 * Contrat HTTP : {@code /api/v1/network/**}. Les tokens viennent de Keycloak (ou de l'ancien login payment-2).
 */
@SpringBootApplication
public class NetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkApplication.class, args);
    }
}
