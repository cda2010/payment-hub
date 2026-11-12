package com.gateway.payment.divers.security;

import com.gateway.payment.divers.api.CurrentUser;
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
import java.util.UUID;

/**
 * Accepte les access tokens Keycloak (JWKS) et, pendant la bascule, les anciens tokens payment-2
 * (HMAC, même secret et même issuer). Le propriétaire d'un coffre ou d'une tontine est le
 * {@code preferred_username}, à défaut le subject.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> TECHNICAL_ROLES = Set.of("offline_access", "uma_authorization");

    private final SecretKey legacyKey;
    private final String legacyIssuer;
    private final String keycloakIssuer;
    private final JwtDecoder keycloakDecoder;

    @Autowired
    public JwtAuthenticationFilter(DiversSecurityProperties properties) {
        this(properties, properties.keycloak().enabled() ? buildDecoder(properties.keycloak()) : null);
    }

    JwtAuthenticationFilter(DiversSecurityProperties properties, JwtDecoder keycloakDecoder) {
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
            String token = header.substring(7);
            UsernamePasswordAuthenticationToken authentication = authenticate(token);
            if (authentication != null) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(CurrentUser.PARTY_ID, partyId(token));
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
        return authentication(claims.getSubject(), claims.get("roles") instanceof Collection<?> roles ? roles : List.of());
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

    private String partyId(String token) {
        try {
            Object raw = JWTParser.parse(token).getJWTClaimsSet().getClaim("party_id");
            if (raw == null || !StringUtils.hasText(String.valueOf(raw))) {
                return null;
            }
            return UUID.fromString(String.valueOf(raw).trim()).toString();
        } catch (Exception e) {
            return null;
        }
    }

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

    private static JwtDecoder buildDecoder(DiversSecurityProperties.Keycloak keycloak) {
        if (!StringUtils.hasText(keycloak.issuerUri())) {
            throw new IllegalStateException("divers.security.keycloak.issuer-uri est requis quand Keycloak est activé");
        }
        String jwkSetUri = StringUtils.hasText(keycloak.jwkSetUri())
                ? keycloak.jwkSetUri()
                : keycloak.issuerUri().replaceAll("/+$", "") + "/protocol/openid-connect/certs";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(validator(keycloak));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> validator(DiversSecurityProperties.Keycloak keycloak) {
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
