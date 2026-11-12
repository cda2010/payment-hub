package com.gateway.payment.divers.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jwt.secret} et {@code jwt.issuer} doivent rester identiques à ceux de payment-2
 * pour accepter les anciens tokens HMAC pendant la bascule Keycloak.
 */
@ConfigurationProperties(prefix = "divers.security")
public record DiversSecurityProperties(Jwt jwt, Cors cors, Keycloak keycloak) {

    public DiversSecurityProperties {
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
