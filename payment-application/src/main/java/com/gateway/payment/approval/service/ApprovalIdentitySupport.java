package com.gateway.payment.approval.service;

import com.gateway.payment.approval.config.ApprovalIdentityProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ApprovalIdentitySupport {

    private final ApprovalIdentityProperties properties;

    public ApprovalIdentitySupport(ApprovalIdentityProperties properties) {
        this.properties = properties;
    }

    public Set<String> currentRoleTokens() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Set.of();
        }
        return normalizeAuthorities(auth.getAuthorities());
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String name = auth.getName();
        if (!StringUtils.hasText(name) || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name.trim();
    }

    public boolean isAdmin() {
        Set<String> roles = currentRoleTokens();
        return roles.contains("ADMIN") || roles.contains("ROLE_ADMIN");
    }

    public boolean seesAllInbox() {
        return properties.isAdminSeesAll() && isAdmin();
    }

    /**
     * True si au moins un candidateGroup (CSV) croise les autorités de l'utilisateur.
     */
    public boolean matchesCandidateGroups(String candidateGroupsCsv) {
        if (!StringUtils.hasText(candidateGroupsCsv)) {
            return seesAllInbox();
        }
        if (seesAllInbox()) {
            return true;
        }
        Set<String> userRoles = currentRoleTokens();
        if (userRoles.isEmpty()) {
            return false;
        }
        for (String g : candidateGroupsCsv.split(",")) {
            if (!StringUtils.hasText(g)) {
                continue;
            }
            String token = normalizeToken(g.trim());
            if (userRoles.contains(token)
                    || userRoles.contains(properties.getRolePrefix() + token)
                    || userRoles.contains("ROLE_" + token)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> normalizeAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (GrantedAuthority a : authorities) {
            if (a == null || !StringUtils.hasText(a.getAuthority())) {
                continue;
            }
            String raw = a.getAuthority().trim();
            out.add(raw);
            out.add(normalizeToken(raw));
            String prefix = properties.getRolePrefix();
            if (StringUtils.hasText(prefix) && raw.startsWith(prefix)) {
                out.add(normalizeToken(raw.substring(prefix.length())));
            }
            if (raw.startsWith("ROLE_")) {
                out.add(normalizeToken(raw.substring(5)));
            }
        }
        return out;
    }

    private static String normalizeToken(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public Set<String> normalizeGroups(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String g : csv.split(",")) {
            if (StringUtils.hasText(g)) {
                out.add(normalizeToken(g));
            }
        }
        return out;
    }

    public String rolesSummary() {
        return currentRoleTokens().stream().sorted().collect(Collectors.joining(","));
    }
}
