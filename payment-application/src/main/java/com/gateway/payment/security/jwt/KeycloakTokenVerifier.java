package com.gateway.payment.security.jwt;

import com.gateway.payment.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vérifie les access tokens Keycloak (signature via JWKS, issuer, expiration, audience)
 * et les convertit en {@link KeycloakUserDetails}.
 */
@Component
public class KeycloakTokenVerifier {

    private static final Set<String> TECHNICAL_ROLES = Set.of("offline_access", "uma_authorization");

    private final SecurityProperties.Keycloak properties;
    private final JwtDecoder decoder;

    @Autowired
    public KeycloakTokenVerifier(SecurityProperties securityProperties) {
        this.properties = securityProperties.keycloak();
        this.decoder = properties.enabled() ? buildDecoder(properties) : null;
    }

    KeycloakTokenVerifier(SecurityProperties.Keycloak properties, JwtDecoder decoder) {
        this.properties = properties;
        this.decoder = decoder;
    }

    public boolean handles(String issuer) {
        return decoder != null && issuer != null && issuer.equals(properties.issuerUri());
    }

    /** @throws org.springframework.security.oauth2.jwt.JwtException si le token est invalide */
    public KeycloakUserDetails verify(String token) {
        return toUserDetails(decoder.decode(token));
    }

    KeycloakUserDetails toUserDetails(Jwt jwt) {
        String username = StringUtils.hasText(jwt.getClaimAsString("preferred_username"))
                ? jwt.getClaimAsString("preferred_username")
                : jwt.getSubject();
        List<SimpleGrantedAuthority> authorities = roles(jwt).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new KeycloakUserDetails(
                username,
                authorities,
                jwt.getSubject(),
                jwt.getClaimAsString("tenant_id"),
                jwt.getClaimAsString("party_id"),
                jwt.getClaimAsString("audience")
        );
    }

    /** Claim {@code roles} (mapper du realm), à défaut {@code realm_access.roles}. */
    private static Set<String> roles(Jwt jwt) {
        Collection<?> raw = jwt.getClaim("roles") instanceof Collection<?> c ? c : null;
        if (raw == null && jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess
                && realmAccess.get("roles") instanceof Collection<?> c) {
            raw = c;
        }
        Set<String> out = new LinkedHashSet<>();
        if (raw != null) {
            for (Object r : raw) {
                String role = r == null ? null : r.toString().trim();
                if (StringUtils.hasText(role)
                        && !TECHNICAL_ROLES.contains(role)
                        && !role.startsWith("default-roles-")) {
                    out.add(role);
                }
            }
        }
        return out;
    }

    private static JwtDecoder buildDecoder(SecurityProperties.Keycloak properties) {
        if (!StringUtils.hasText(properties.issuerUri())) {
            throw new IllegalStateException("gateway.security.keycloak.issuer-uri est requis quand Keycloak est activé");
        }
        String jwkSetUri = StringUtils.hasText(properties.jwkSetUri())
                ? properties.jwkSetUri()
                : properties.issuerUri().replaceAll("/+$", "") + "/protocol/openid-connect/certs";
        // JWKS chargé à la première requête : le backend démarre même si Keycloak est arrêté.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(validator(properties));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> validator(SecurityProperties.Keycloak properties) {
        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(properties.issuerUri());
        if (!StringUtils.hasText(properties.audience())) {
            return issuer;
        }
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null
                && jwt.getAudience().contains(properties.audience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Audience attendue : " + properties.audience(), null));
        return new DelegatingOAuth2TokenValidator<>(issuer, audience);
    }
}
