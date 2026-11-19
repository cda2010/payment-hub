package com.gateway.payment.identity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.identity.enrolement.EnrollmentProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Ferme une session Keycloak (en ligne ou hors ligne) identifiée par le claim {@code sid}. */
@Component
public class SessionsKeycloak {

    private final EnrollmentProperties enrollment;
    private final ObjectMapper json;
    private final RestClient http = RestClient.create();

    public SessionsKeycloak(EnrollmentProperties enrollment, ObjectMapper json) {
        this.enrollment = enrollment;
        this.json = json;
    }

    public void revoquer(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            http.delete()
                    .uri(admin("/sessions/" + sessionId.trim()))
                    .header("Authorization", "Bearer " + jetonAdmin())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return;
            }
            throw new IllegalStateException("La session de l'appareil n'a pas été fermée");
        } catch (IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalStateException("La session de l'appareil n'a pas été fermée");
        }
    }

    private String jetonAdmin() {
        LinkedMultiValueMap<String, String> formulaire = new LinkedMultiValueMap<>();
        formulaire.add("client_id", "admin-cli");
        formulaire.add("grant_type", "password");
        formulaire.add("username", enrollment.getKeycloakAdmin());
        formulaire.add("password", enrollment.getKeycloakAdminPassword());
        try {
            String corps = http.post()
                    .uri(enrollment.getKeycloakUrl().replaceAll("/+$", "")
                            + "/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulaire)
                    .retrieve()
                    .body(String.class);
            return json.readTree(corps).get("access_token").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Le service de connexion est injoignable");
        }
    }

    private String admin(String chemin) {
        return enrollment.getKeycloakUrl().replaceAll("/+$", "")
                + "/admin/realms/" + enrollment.getKeycloakRealm() + chemin;
    }
}
