package com.gateway.payment.edge;

import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Vérifie la signature, l'émetteur et l'expiration. Ni le rôle, ni l'audience, ni l'unité.
 */
@Component
class JetonVerifier {

    private final EdgeSecurityProperties properties;
    private final SecretKey legacyKey;
    private volatile NimbusJwtDecoder keycloak;

    JetonVerifier(EdgeSecurityProperties properties) {
        this.properties = properties;
        this.legacyKey = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    boolean signatureValide(String token) {
        try {
            String issuer = issuer(token);
            if (!StringUtils.hasText(issuer)) {
                return false;
            }
            if (properties.getKeycloak().isEnabled() && issuer.equals(properties.getKeycloak().getIssuerUri())) {
                return keycloak().decode(token) != null;
            }
            if (!issuer.equals(properties.getJwt().getIssuer())) {
                return false;
            }
            Claims claims = Jwts.parser()
                    .verifyWith(legacyKey)
                    .requireIssuer(properties.getJwt().getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "access".equals(claims.get("type", String.class));
        } catch (RuntimeException | ParseException ex) {
            return false;
        }
    }

    private static String issuer(String token) throws ParseException {
        return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
    }

    private NimbusJwtDecoder keycloak() {
        NimbusJwtDecoder current = keycloak;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (keycloak == null) {
                String jwk = StringUtils.hasText(properties.getKeycloak().getJwkSetUri())
                        ? properties.getKeycloak().getJwkSetUri()
                        : properties.getKeycloak().getIssuerUri().replaceAll("/+$", "")
                        + "/protocol/openid-connect/certs";
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwk).build();
                decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getKeycloak().getIssuerUri()));
                keycloak = decoder;
            }
            return keycloak;
        }
    }
}
