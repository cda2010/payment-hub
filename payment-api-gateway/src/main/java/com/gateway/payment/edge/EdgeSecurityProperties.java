package com.gateway.payment.edge;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Secrets et émetteur. Le rôle, l'audience et l'unité ne sont pas lus ici.
 */
@ConfigurationProperties(prefix = "edge.security")
public class EdgeSecurityProperties {

    private final Keycloak keycloak = new Keycloak();
    private final Jwt jwt = new Jwt();

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public static class Keycloak {
        private boolean enabled = true;
        private String issuerUri = "http://localhost:8180/realms/payment";
        private String jwkSetUri = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }
    }

    public static class Jwt {
        private String secret = "change-me-to-a-very-long-secret-key-at-least-256-bits";
        private String issuer = "payment-gateway";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}
