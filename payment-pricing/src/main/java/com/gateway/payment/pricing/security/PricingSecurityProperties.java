package com.gateway.payment.pricing.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jwt.secret} et {@code jwt.issuer} doivent être identiques à
 * {@code gateway.security.jwt.*} de payment-2, qui émet les anciens tokens.
 */
@ConfigurationProperties(prefix = "pricing.security")
public record PricingSecurityProperties(Jwt jwt, Cors cors, Keycloak keycloak) {

    public PricingSecurityProperties {
        if (keycloak == null) {
            keycloak = new Keycloak(false, null, null, null);
        }
    }

    public record Jwt(String secret, String issuer) {
    }

    public record Cors(String allowedOrigins) {
    }

    /**
     * @param issuerUri valeur exacte du claim {@code iss}
     * @param jwkSetUri vide = {@code issuerUri + /protocol/openid-connect/certs}
     * @param audience  valeur attendue dans {@code aud}
     */
    public record Keycloak(boolean enabled, String issuerUri, String jwkSetUri, String audience) {
    }
}
