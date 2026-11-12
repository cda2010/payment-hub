package com.gateway.payment.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Référentiel Identité — application autonome, base dédiée.
 * Contrat HTTP : {@code /api/v1/identity/**}. Les JWT sont émis par payment-2.
 */
@SpringBootApplication
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
