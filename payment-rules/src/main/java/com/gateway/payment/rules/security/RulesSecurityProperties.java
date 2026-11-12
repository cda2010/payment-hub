package com.gateway.payment.rules.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rules.security")
public record RulesSecurityProperties(Jwt jwt, Cors cors, Keycloak keycloak) {

    public RulesSecurityProperties {
        if (keycloak == null) {
            keycloak = new Keycloak(false, null, null, null);
        }
    }

    public record Jwt(String secret, String issuer) {
    }

    public record Cors(String allowedOrigins) {
    }

    public record Keycloak(boolean enabled, String issuerUri, String jwkSetUri, String audience) {
    }
}
