package com.gateway.payment.fidelite.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jwt.secret} et {@code jwt.issuer} doivent rester identiques à ceux de payment-2
 * pour accepter les anciens tokens HMAC pendant la bascule Keycloak.
 */
@ConfigurationProperties(prefix = "fidelite.security")
public record FideliteSecurityProperties(Jwt jwt, Cors cors, Keycloak keycloak) {

    public FideliteSecurityProperties {
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
