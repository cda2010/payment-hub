package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.identity.security.IdentitySecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Alias WALLET du compte Coris Money déjà connu, propriétaire = la personne. */
@Component
public class AliasCompte implements AliasPortefeuille {

    private static final Logger log = LoggerFactory.getLogger(AliasCompte.class);

    private final EnrollmentProperties enrollment;
    private final IdentitySecurityProperties security;
    private final ObjectMapper json;
    private final RestClient http;

    public AliasCompte(EnrollmentProperties enrollment, IdentitySecurityProperties security, ObjectMapper json) {
        this.enrollment = enrollment;
        this.security = security;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public void assurer(String tenantId, UUID partyId, String numero, String libelle) {
        String url = enrollment.getAliasUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Le service d'alias n'est pas configuré");
        }
        String compte = numero.trim();
        String base = url.replaceAll("/+$", "");
        String nom = libelle == null || libelle.isBlank() ? compte : libelle.trim();
        String code = "ALIAS-CM-" + compte;
        if (appartientDeja(base, tenantId, code, partyId)) {
            return;
        }
        if (existe(base, tenantId, code)) {
            code = compte;
            if (appartientDeja(base, tenantId, code, partyId) || existe(base, tenantId, code)) {
                return;
            }
        }
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("tenantId", tenantId);
        corps.put("aliasCode", code);
        corps.put("label", nom);
        corps.put("aliasType", "WALLET");
        corps.put("externalRef", compte);
        corps.put("currency", "XOF");
        corps.put("ownerKind", "PARTY");
        corps.put("ownerId", partyId.toString());
        corps.put("providerCode", "0011");
        try {
            http.post()
                    .uri(base)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corps)
                    .retrieve()
                    .toBodilessEntity();
            log.info("alias {} créé pour {}", code, partyId);
        } catch (RestClientResponseException e) {
            String message = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 400 && message != null && message.toLowerCase().contains("already exists")) {
                return;
            }
            throw new IllegalStateException("L'alias du compte n'a pas été créé");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le service d'alias est injoignable");
        }
    }

    private boolean appartientDeja(String base, String tenantId, String code, UUID partyId) {
        JsonNode alias = lire(base, tenantId, code);
        return alias != null && partyId.toString().equals(alias.path("ownerId").asText());
    }

    private boolean existe(String base, String tenantId, String code) {
        return lire(base, tenantId, code) != null;
    }

    private JsonNode lire(String base, String tenantId, String code) {
        try {
            String reponse = http.get()
                    .uri(base + "/resolve/" + code + "?tenantId=" + tenantId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton())
                    .retrieve()
                    .body(String.class);
            return json.readTree(reponse);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw new IllegalStateException("L'alias du compte n'a pas pu être lu");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le service d'alias est injoignable");
        }
    }

    private String jeton() {
        SecretKey cle = Keys.hmacShaKeyFor(security.jwt().secret().getBytes(StandardCharsets.UTF_8));
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject("enrolement")
                .issuer(security.jwt().issuer())
                .claim("type", "access")
                .claim("roles", List.of("OPERATOR"))
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plusSeconds(120)))
                .signWith(cle)
                .compact();
    }
}
