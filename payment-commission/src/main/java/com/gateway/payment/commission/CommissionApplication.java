package com.gateway.payment.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Moteur de commissionnement — application autonome, base dédiée.
 * Contrat HTTP : {@code /api/v1/commission/**}.
 */
@SpringBootApplication
public class CommissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionApplication.class, args);
    }
}
