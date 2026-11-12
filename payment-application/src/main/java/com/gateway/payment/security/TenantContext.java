package com.gateway.payment.security;

import com.gateway.payment.security.jwt.KeycloakUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/** Accès au tenant et aux rôles plateforme depuis le SecurityContext. */
public final class TenantContext {

    private static final Set<String> PLATFORM_ROLES = Set.of(
            "ROLE_ADMIN", "ROLE_SUPPORT", "ROLE_AUDITOR", "ROLE_OPERATOR", "ROLE_CONFIG_MANAGER");

    private TenantContext() {}

    public static String currentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof KeycloakUserDetails kud) {
            String t = kud.getTenantId();
            return t == null || t.isBlank() ? null : t.trim();
        }
        return null;
    }

    public static boolean isPlatformUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (PLATFORM_ROLES.contains(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /** Username Keycloak ({@code preferred_username} / principal name). */
    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name.trim();
    }

    /**
     * Accès tenant :
     * <ul>
     *   <li>plateforme → OK</li>
     *   <li>ressource sans tenant (legacy) → OK</li>
     *   <li>user avec tenant_id → égalité stricte</li>
     *   <li>user sans tenant_id (JWT legacy) → OK (compat) ; à retirer quand tous les tokens portent le claim</li>
     * </ul>
     */
    public static boolean canAccess(String resourceTenantId) {
        if (isPlatformUser()) {
            return true;
        }
        if (resourceTenantId == null || resourceTenantId.isBlank()) {
            return true;
        }
        String userTenant = currentTenantId();
        if (userTenant == null || userTenant.isBlank()) {
            return true;
        }
        return userTenant.equals(resourceTenantId);
    }

    /** Strict : refuse si user sans tenant sur ressource tenantée. */
    public static boolean canAccessStrict(String resourceTenantId) {
        if (isPlatformUser()) {
            return true;
        }
        if (resourceTenantId == null || resourceTenantId.isBlank()) {
            return true;
        }
        String userTenant = currentTenantId();
        if (userTenant == null || userTenant.isBlank()) {
            return false;
        }
        return userTenant.equals(resourceTenantId);
    }

    /** Claim {@code party_id} du JWT Keycloak, si présent. */
    public static String currentPartyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof KeycloakUserDetails kud)) {
            return null;
        }
        String p = kud.getPartyId();
        return p == null || p.isBlank() ? null : p.trim();
    }
}
