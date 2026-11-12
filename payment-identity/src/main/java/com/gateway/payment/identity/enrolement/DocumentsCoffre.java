package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.identity.security.IdentitySecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DocumentsCoffre implements CoffreDocuments {

    private final EnrollmentProperties enrollment;
    private final IdentitySecurityProperties security;
    private final ObjectMapper json;
    private final RestClient http;

    public DocumentsCoffre(
            EnrollmentProperties enrollment,
            IdentitySecurityProperties security,
            ObjectMapper json
    ) {
        this.enrollment = enrollment;
        this.security = security;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public UUID deposer(
            String tenantId,
            UUID dossierId,
            String face,
            String typeDocument,
            String contentType,
            byte[] contenu
    ) {
        String url = enrollment.getDocumentsUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Le coffre documentaire n'est pas configuré");
        }
        String type = normaliser(contentType);
        ByteArrayResource fichier = new ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return face.toLowerCase() + extension(type);
            }
        };
        HttpHeaders fichierEntetes = new HttpHeaders();
        fichierEntetes.setContentType(MediaType.parseMediaType(type));
        HttpHeaders metaEntetes = new HttpHeaders();
        metaEntetes.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> meta = Map.of(
                "tenantId", tenantId,
                "code", typeDocument + "_" + face,
                "ownerKind", "ENROLEMENT",
                "ownerId", dossierId.toString()
        );
        LinkedMultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("file", new org.springframework.http.HttpEntity<>(fichier, fichierEntetes));
        corps.add("meta", new org.springframework.http.HttpEntity<>(meta, metaEntetes));
        try {
            String reponse = http.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(corps)
                    .retrieve()
                    .body(String.class);
            JsonNode noeud = json.readTree(reponse);
            return UUID.fromString(noeud.get("id").asText());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le coffre documentaire n'a pas enregistré la pièce");
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

    private static String normaliser(String contentType) {
        if (contentType == null || contentType.isBlank() || "image/jpg".equalsIgnoreCase(contentType)) {
            return "image/jpeg";
        }
        return contentType.trim().toLowerCase();
    }

    private static String extension(String type) {
        return switch (type) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
