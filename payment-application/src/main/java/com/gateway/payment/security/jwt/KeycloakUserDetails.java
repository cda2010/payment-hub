package com.gateway.payment.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Utilisateur authentifié par un token Keycloak. Pas de ligne {@code gateway_user} :
 * nom et rôles viennent du token.
 */
public class KeycloakUserDetails extends User {

    private final String subject;
    private final String tenantId;
    private final String partyId;
    private final String audience;

    public KeycloakUserDetails(
            String username,
            Collection<? extends GrantedAuthority> authorities,
            String subject,
            String tenantId,
            String partyId,
            String audience
    ) {
        super(username, "", authorities);
        this.subject = subject;
        this.tenantId = tenantId;
        this.partyId = partyId;
        this.audience = audience;
    }

    /** Identifiant Keycloak stable ({@code sub}). */
    public String getSubject() {
        return subject;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getAudience() {
        return audience;
    }
}
