package com.gateway.payment.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "gateway.security")
public record SecurityProperties(
        Jwt jwt,
        Cors cors,
        Keycloak keycloak
) {
    public SecurityProperties {
        if (keycloak == null) {
            keycloak = new Keycloak(false, null, null, null);
        }
    }

    public record Jwt(
            String secret,
            String issuer,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {}

    public record Cors(String allowedOrigins) {}

    /**
     * Tokens émis par Keycloak, acceptés en plus des tokens maison ({@link Jwt}).
     *
     * @param issuerUri  valeur exacte du claim {@code iss} (URL publique du realm)
     * @param jwkSetUri  URL des clés ; vide = {@code issuerUri + /protocol/openid-connect/certs}
     *                   (à renseigner quand le backend joint Keycloak par une autre adresse que l'issuer)
     * @param audience   valeur attendue dans {@code aud}
     */
    public record Keycloak(
            boolean enabled,
            String issuerUri,
            String jwkSetUri,
            String audience
    ) {}
}
