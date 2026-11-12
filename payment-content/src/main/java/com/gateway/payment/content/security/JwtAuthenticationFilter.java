package com.gateway.payment.content.security;

import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Accepte les access tokens Keycloak (JWKS) et, pendant la bascule, les anciens tokens payment-2
 * (HMAC, même secret et même issuer). Pas de base utilisateurs ici : nom et rôles viennent du token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> TECHNICAL_ROLES = Set.of("offline_access", "uma_authorization");

    private final SecretKey legacyKey;
    private final String legacyIssuer;
    private final String keycloakIssuer;
    private final JwtDecoder keycloakDecoder;

    @Autowired
    public JwtAuthenticationFilter(ContentSecurityProperties properties) {
        this(properties, properties.keycloak().enabled() ? buildDecoder(properties.keycloak()) : null);
    }

    JwtAuthenticationFilter(ContentSecurityProperties properties, JwtDecoder keycloakDecoder) {
        this.legacyKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.legacyIssuer = properties.jwt().issuer();
        this.keycloakIssuer = properties.keycloak().issuerUri();
        this.keycloakDecoder = keycloakDecoder;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = authenticate(header.substring(7));
            if (authentication != null) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken authenticate(String token) throws Exception {
        String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        if (keycloakDecoder != null && Objects.equals(issuer, keycloakIssuer)) {
            Jwt jwt = keycloakDecoder.decode(token);
            String username = StringUtils.hasText(jwt.getClaimAsString("preferred_username"))
                    ? jwt.getClaimAsString("preferred_username")
                    : jwt.getSubject();
            return authentication(username, keycloakRoles(jwt));
        }

        Claims claims = Jwts.parser()
                .verifyWith(legacyKey)
                .requireIssuer(legacyIssuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!"access".equals(claims.get("type", String.class))) {
            return null;
        }
        return authentication(claims.getSubject(), claims.get("roles") instanceof Collection<?> c ? c : List.of());
    }

    private static UsernamePasswordAuthenticationToken authentication(String username, Collection<?> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(role -> !TECHNICAL_ROLES.contains(role) && !role.startsWith("default-roles-"))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    /** Claim {@code roles} (mapper du realm), à défaut {@code realm_access.roles}. */
    private static Collection<?> keycloakRoles(Jwt jwt) {
        if (jwt.getClaim("roles") instanceof Collection<?> roles) {
            return roles;
        }
        if (jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess
                && realmAccess.get("roles") instanceof Collection<?> roles) {
            return roles;
        }
        return List.of();
    }

    private static JwtDecoder buildDecoder(ContentSecurityProperties.Keycloak keycloak) {
        if (!StringUtils.hasText(keycloak.issuerUri())) {
            throw new IllegalStateException("content.security.keycloak.issuer-uri est requis quand Keycloak est activé");
        }
        String jwkSetUri = StringUtils.hasText(keycloak.jwkSetUri())
                ? keycloak.jwkSetUri()
                : keycloak.issuerUri().replaceAll("/+$", "") + "/protocol/openid-connect/certs";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(validator(keycloak));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> validator(ContentSecurityProperties.Keycloak keycloak) {
        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(keycloak.issuerUri());
        if (!StringUtils.hasText(keycloak.audience())) {
            return issuer;
        }
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null
                && jwt.getAudience().contains(keycloak.audience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Audience attendue : " + keycloak.audience(), null));
        return new DelegatingOAuth2TokenValidator<>(issuer, audience);
    }
}
