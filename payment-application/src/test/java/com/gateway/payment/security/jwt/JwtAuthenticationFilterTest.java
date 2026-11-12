package com.gateway.payment.security.jwt;

import com.gateway.payment.security.config.SecurityProperties;
import com.gateway.payment.security.domain.GatewayRole;
import com.gateway.payment.security.domain.GatewayUser;
import com.gateway.payment.security.service.GatewayUserDetailsService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String KC_ISSUER = "http://localhost:8180/realms/payment";
    private static final SecurityProperties PROPS = new SecurityProperties(
            new SecurityProperties.Jwt("test-secret-test-secret-test-secret-test-secret", "payment-gateway",
                    Duration.ofMinutes(15), Duration.ofDays(7)),
            new SecurityProperties.Cors("http://localhost:4200"),
            new SecurityProperties.Keycloak(true, KC_ISSUER, null, "payment-api")
    );

    private static KeyPair keycloakKeys;
    private static KeyPair otherKeys;

    private final GatewayUserDetailsService userDetailsService = mock(GatewayUserDetailsService.class);
    private final JwtService jwtService = new JwtService(PROPS);

    @BeforeAll
    static void keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keycloakKeys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void keycloakTokenBuildsUserDetailsFromClaims() throws Exception {
        Authentication auth = run(keycloakToken(keycloakKeys, KC_ISSUER, List.of("payment-api", "account"),
                List.of("default-roles-payment", "GROUP_ADMIN", "offline_access", "uma_authorization")));

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("jean");
        assertThat(roles(auth)).containsExactly("ROLE_GROUP_ADMIN");
        KeycloakUserDetails user = (KeycloakUserDetails) auth.getPrincipal();
        assertThat(user.getTenantId()).isEqualTo("abc-holding");
        assertThat(user.getAudience()).isEqualTo("CORPORATE");
        assertThat(user.getSubject()).isEqualTo("kc-sub-jean");
    }

    @Test
    void keycloakTokenWithoutApiAudienceIsRejected() throws Exception {
        assertThat(run(keycloakToken(keycloakKeys, KC_ISSUER, List.of("account"), List.of("ADMIN")))).isNull();
    }

    @Test
    void keycloakTokenSignedWithAnotherKeyIsRejected() throws Exception {
        assertThat(run(keycloakToken(otherKeys, KC_ISSUER, List.of("payment-api"), List.of("ADMIN")))).isNull();
    }

    @Test
    void tokenFromUnknownIssuerIsRejected() throws Exception {
        assertThat(run(keycloakToken(keycloakKeys, "http://evil/realms/payment", List.of("payment-api"),
                List.of("ADMIN")))).isNull();
    }

    @Test
    void legacyAccessTokenStillLoadsGatewayUser() {
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
                User.withUsername("admin").password("x").roles("ADMIN").build());

        Authentication auth = run(jwtService.createAccessToken(gatewayUser()));

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("admin");
        assertThat(roles(auth)).containsExactly("ROLE_ADMIN");
    }

    @Test
    void legacyRefreshTokenIsNotAccepted() {
        assertThat(run(jwtService.createRefreshToken(gatewayUser()))).isNull();
    }

    @Test
    void keycloakDisabledFallsBackToLegacyOnly() throws Exception {
        KeycloakTokenVerifier disabled = new KeycloakTokenVerifier(
                new SecurityProperties.Keycloak(false, KC_ISSUER, null, "payment-api"), null);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, disabled);

        assertThat(run(filter, keycloakToken(keycloakKeys, KC_ISSUER, List.of("payment-api"), List.of("ADMIN"))))
                .isNull();
    }

    private Authentication run(String token) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keycloakKeys.getPublic()).build();
        decoder.setJwtValidator(KeycloakTokenVerifier.validator(PROPS.keycloak()));
        KeycloakTokenVerifier verifier = new KeycloakTokenVerifier(PROPS.keycloak(), decoder);
        return run(new JwtAuthenticationFilter(jwtService, userDetailsService, verifier), token);
    }

    private static Authentication run(JwtAuthenticationFilter filter, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payments");
        request.addHeader("Authorization", "Bearer " + token);
        try {
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static String keycloakToken(KeyPair keys, String issuer, List<String> audience, List<String> roles)
            throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("kc-sub-jean")
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString())
                .claim("preferred_username", "jean")
                .claim("roles", roles)
                .claim("realm_access", Map.of("roles", roles))
                .claim("tenant_id", "abc-holding")
                .claim("audience", "CORPORATE")
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(keys.getPrivate()));
        return jwt.serialize();
    }

    private static GatewayUser gatewayUser() {
        GatewayRole role = mock(GatewayRole.class);
        when(role.getCode()).thenReturn("ADMIN");
        GatewayUser user = mock(GatewayUser.class);
        when(user.getUsername()).thenReturn("admin");
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getRoles()).thenReturn(Set.of(role));
        return user;
    }

    private static List<String> roles(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
