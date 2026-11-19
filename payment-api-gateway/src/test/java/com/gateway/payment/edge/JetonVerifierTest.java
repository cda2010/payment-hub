package com.gateway.payment.edge;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JetonVerifierTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long";

    @Test
    void accepteUnJetonSigneEtRefuseUneSignatureFausse() {
        EdgeSecurityProperties properties = properties();
        JetonVerifier verifier = new JetonVerifier(properties);

        assertTrue(verifier.signatureValide(signe(SECRET, "access")));
        assertFalse(verifier.signatureValide(signe("autre-secret-au-moins-32-bytes!!", "access")));
        assertFalse(verifier.signatureValide(signe(SECRET, "refresh")));
        assertFalse(verifier.signatureValide("pas-un-jeton"));
    }

    @Test
    void laisseOuvertsLesCheminsSansSession() {
        assertTrue(CheminsPublics.ouvert("GET", "/api/v1/identity/enrolement/indicatifs"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/identity/enrolement/demandes"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/auth/token"));
        assertTrue(CheminsPublics.ouvert("GET", "/api/v1/ping"));
        assertTrue(CheminsPublics.ouvert("GET", "/api/v1/i18n/fr"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/webhooks/oml"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/external-omnicanal/boundary"));
        assertTrue(CheminsPublics.ouvert("GET", "/api/v1/network/fse"));
        assertTrue(CheminsPublics.ouvert("GET", "/api/v1/content/media/logo.png"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/boundaryByTypeMethode"));
        assertTrue(CheminsPublics.ouvert("POST", "/api/v1/canal/session"));
        assertTrue(CheminsPublics.ouvert("OPTIONS", "/api/v1/fidelite/programmes"));

        assertFalse(CheminsPublics.ouvert("GET", "/api/v1/fidelite/programmes"));
        assertFalse(CheminsPublics.ouvert("POST", "/api/v1/network/fse"));
        assertFalse(CheminsPublics.ouvert("GET", "/api/v1/external-omnicanal/boundary"));
        assertFalse(CheminsPublics.ouvert("POST", "/api/v1/identity/moi"));
        assertFalse(CheminsPublics.ouvert("GET", "/api/v1/content/pages"));
    }

    private static EdgeSecurityProperties properties() {
        EdgeSecurityProperties properties = new EdgeSecurityProperties();
        properties.getKeycloak().setEnabled(false);
        properties.getJwt().setSecret(SECRET);
        properties.getJwt().setIssuer("payment-gateway");
        return properties;
    }

    private static String signe(String secret, String type) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("poste")
                .issuer("payment-gateway")
                .claim("type", type)
                .expiration(Date.from(Instant.now().plusSeconds(120)))
                .signWith(key)
                .compact();
    }
}
