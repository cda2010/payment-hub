package com.gateway.payment.fidelite.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class CurrentUser {

    public static final String PARTY_ID = "partyId";
    public static final String TENANT_ID = "tenantId";

    private CurrentUser() {
    }

    public static String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("Authentification requise");
        }
        return authentication.getName();
    }

    public static UUID partyId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        Object raw = attrs.getRequest().getAttribute(PARTY_ID);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(raw).trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Claim {@code tenant_id} du jeton. Vide pour le jeton démo, le service retombe alors sur CORIS_DEMO. */
    public static String tenantId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        Object raw = attrs.getRequest().getAttribute(TENANT_ID);
        if (raw == null || String.valueOf(raw).isBlank()) {
            return null;
        }
        return String.valueOf(raw).trim();
    }
}
