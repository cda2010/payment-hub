package com.gateway.payment.fidelite.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

public final class CurrentUser {

    public static final String PARTY_ID = "partyId";
    public static final String TENANT_ID = "tenantId";

    private static final Set<String> EXPLOITATION = Set.of(
            "ROLE_ADMIN",
            "ROLE_DIRECTEUR",
            "ROLE_RESPONSABLE_OPERATIONS",
            "ROLE_CHEF_AGENCE",
            "ROLE_AUDITOR",
            "ROLE_OPERATOR",
            "ROLE_CONFIG_MANAGER");

    private CurrentUser() {
    }

    /** Liste globale des parrainages : réservée aux rôles d'exploitation du backoffice. */
    public static void exigerExploitation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean autorise = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> EXPLOITATION.contains(authority.getAuthority()));
        if (!autorise) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé à l'exploitation");
        }
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
