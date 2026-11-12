package com.gateway.payment.pricing.security;

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
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String KC_ISSUER = "http://localhost:8180/realms/payment";
    private static final PricingSecurityProperties PROPS = new PricingSecurityProperties(
            new PricingSecurityProperties.Jwt("test-secret-test-secret-test-secret-test-secret", "payment-gateway"),
            new PricingSecurityProperties.Cors("http://localhost:4210"),
            new PricingSecurityProperties.Keycloak(true, KC_ISSUER, null, "payment-api")
    );

    private static KeyPair keys;
    private static KeyPair otherKeys;

    @BeforeAll
    static void keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void keycloakTokenUsesPreferredUsernameAndRealmRoles() throws Exception {
        Authentication auth = run(token(keys, List.of("payment-api"),
                List.of("default-roles-payment", "CUSTOMER", "offline_access")));

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("awa");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void keycloakTokenWithoutApiAudienceIsRejected() throws Exception {
        assertThat(run(token(keys, List.of("account"), List.of("ADMIN")))).isNull();
    }

    @Test
    void keycloakTokenSignedWithAnotherKeyIsRejected() throws Exception {
        assertThat(run(token(otherKeys, List.of("payment-api"), List.of("ADMIN")))).isNull();
    }

    private static Authentication run(String token) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic()).build();
        decoder.setJwtValidator(JwtAuthenticationFilter.validator(PROPS.keycloak()));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(PROPS, decoder);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pricing/policies");
        request.addHeader("Authorization", "Bearer " + token);
        try {
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static String token(KeyPair signingKeys, List<String> audience, List<String> roles) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(KC_ISSUER)
                .subject("kc-sub-awa")
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .claim("preferred_username", "awa")
                .claim("roles", roles)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(signingKeys.getPrivate()));
        return jwt.serialize();
    }
}
