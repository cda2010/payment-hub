package com.m2i.pti.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jwt.JWTParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.text.ParseException;

/**
 * Jeton local pour le client AngularJS, et jeton Keycloak pour le backoffice.
 */
@Configuration
public class PtiJwtConfiguration {

    public static final String ISSUER = "pti";
    static final String KEYCLOAK_ISSUER = "http://localhost:8180/realms/payment";

    @Bean
    public SecretKey ptiJwtKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey ptiJwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(ptiJwtKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey ptiJwtKey) {
        JwtDecoder local = NimbusJwtDecoder.withSecretKey(ptiJwtKey).build();
        return token -> {
            String issuer = issuer(token);
            if (issuer != null && issuer.contains("/realms/")) {
                return keycloakDecoder().decode(token);
            }
            return local.decode(token);
        };
    }

    private static volatile JwtDecoder keycloak;

    private static JwtDecoder keycloakDecoder() {
        JwtDecoder current = keycloak;
        if (current == null) {
            synchronized (PtiJwtConfiguration.class) {
                current = keycloak;
                if (current == null) {
                    current = JwtDecoders.fromIssuerLocation(KEYCLOAK_ISSUER);
                    keycloak = current;
                }
            }
        }
        return current;
    }

    private static String issuer(String token) {
        try {
            return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException ex) {
            return null;
        }
    }
}
