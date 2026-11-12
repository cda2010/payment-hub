package com.gateway.payment.approval.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Alignement rôles métier (candidateGroups) ↔ autorités Spring/Keycloak.
 * Ex. groupe {@code CHEF_AGENCE} matche {@code ROLE_CHEF_AGENCE} ou {@code CHEF_AGENCE}.
 */
@ConfigurationProperties(prefix = "gateway.approval.identity")
public class ApprovalIdentityProperties {

    /** Préfixe Spring Security (souvent {@code ROLE_}). */
    private String rolePrefix = "ROLE_";

    /** Si true, un ADMIN voit toute l'inbox. */
    private boolean adminSeesAll = true;

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix != null ? rolePrefix : "ROLE_";
    }

    public boolean isAdminSeesAll() {
        return adminSeesAll;
    }

    public void setAdminSeesAll(boolean adminSeesAll) {
        this.adminSeesAll = adminSeesAll;
    }
}
