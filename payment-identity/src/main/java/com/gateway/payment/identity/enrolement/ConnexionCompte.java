package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.identity.domain.IdentityAudience;
import com.gateway.payment.identity.domain.IdentityChannelUser;
import com.gateway.payment.identity.repository.IdentityChannelUserRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Crée le compte de connexion retail : identifiant = téléphone, mot de passe chez Keycloak. */
@Component
public class ConnexionCompte implements ConnexionIdentite {

    private final EnrollmentProperties enrollment;
    private final IdentityChannelUserRepository utilisateurs;
    private final ObjectMapper json;
    private final RestClient http;

    public ConnexionCompte(
            EnrollmentProperties enrollment,
            IdentityChannelUserRepository utilisateurs,
            ObjectMapper json
    ) {
        this.enrollment = enrollment;
        this.utilisateurs = utilisateurs;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public String identifiant(String tenantId, String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        return utilisateurs.findByTenantIdAndUsername(tenantId, telephone.trim())
                .filter(IdentityChannelUser::isEnabled)
                .map(IdentityChannelUser::getUsername)
                .orElse(null);
    }

    @Override
    public void ouvrir(
            String tenantId,
            UUID partyId,
            String telephone,
            String prenom,
            String nom,
            String email,
            String motDePasse
    ) {
        String identifiant = telephone.trim();
        utilisateurs.findByTenantIdAndUsername(tenantId, identifiant).ifPresent(existant -> {
            if (!partyId.equals(existant.getPartyId())) {
                throw new IllegalArgumentException("Cet identifiant est déjà utilisé");
            }
        });
        String jeton = jetonAdmin();
        String utilisateur = assurerKeycloak(jeton, partyId, identifiant, prenom, nom, email, motDePasse);
        assurerRole(jeton, utilisateur);
        if (utilisateurs.findByTenantIdAndUsername(tenantId, identifiant).isEmpty()) {
            utilisateurs.save(IdentityChannelUser.builder()
                    .tenantId(tenantId)
                    .username(identifiant)
                    .partyId(partyId)
                    .audience(IdentityAudience.RETAIL)
                    .role("CUSTOMER")
                    .enabled(true)
                    .build());
        }
    }

    private String assurerKeycloak(
            String jeton,
            UUID partyId,
            String identifiant,
            String prenom,
            String nom,
            String email,
            String motDePasse
    ) {
        Map<String, Object> corps = representation(partyId, identifiant, prenom, nom, email, motDePasse);
        try {
            String emplacement = http.post()
                    .uri(admin("/users"))
                    .header("Authorization", "Bearer " + jeton)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corps)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders()
                    .getFirst("Location");
            if (emplacement == null || emplacement.isBlank()) {
                throw new IllegalStateException("Le compte de connexion n'a pas été créé");
            }
            return emplacement.substring(emplacement.lastIndexOf('/') + 1);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 409) {
                throw new IllegalStateException("Le compte de connexion n'a pas été créé");
            }
        }
        String id = chercher(jeton, identifiant);
        http.put()
                .uri(admin("/users/" + id))
                .header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON)
                .body(representation(partyId, identifiant, prenom, nom, email, null))
                .retrieve()
                .toBodilessEntity();
        http.put()
                .uri(admin("/users/" + id + "/reset-password"))
                .header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "password", "value", motDePasse, "temporary", false))
                .retrieve()
                .toBodilessEntity();
        return id;
    }

    private void assurerRole(String jeton, String utilisateur) {
        try {
            String role = http.get()
                    .uri(admin("/roles/CUSTOMER"))
                    .header("Authorization", "Bearer " + jeton)
                    .retrieve()
                    .body(String.class);
            JsonNode noeud = json.readTree(role);
            http.post()
                    .uri(admin("/users/" + utilisateur + "/role-mappings/realm"))
                    .header("Authorization", "Bearer " + jeton)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(Map.of("id", noeud.get("id").asText(), "name", "CUSTOMER")))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                return;
            }
            throw new IllegalStateException("Le rôle de connexion n'a pas été accordé");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le rôle de connexion n'a pas été accordé");
        }
    }

    private String chercher(String jeton, String identifiant) {
        try {
            String corps = http.get()
                    .uri(admin("/users?username=" + identifiant + "&exact=true"))
                    .header("Authorization", "Bearer " + jeton)
                    .retrieve()
                    .body(String.class);
            JsonNode liste = json.readTree(corps);
            if (!liste.isArray() || liste.isEmpty()) {
                throw new IllegalStateException("Le compte de connexion n'a pas été créé");
            }
            return liste.get(0).get("id").asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le compte de connexion n'a pas été créé");
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

    private static Map<String, Object> representation(
            UUID partyId,
            String identifiant,
            String prenom,
            String nom,
            String email,
            String motDePasse
    ) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("username", identifiant);
        corps.put("enabled", true);
        corps.put("emailVerified", true);
        if (prenom != null && !prenom.isBlank()) {
            corps.put("firstName", prenom.trim());
        }
        if (nom != null && !nom.isBlank()) {
            corps.put("lastName", nom.trim());
        }
        if (email != null && !email.isBlank()) {
            corps.put("email", email.trim());
        }
        corps.put("attributes", Map.of(
                "audience", List.of("RETAIL"),
                "tenant_id", List.of("CORIS_DEMO"),
                "party_id", List.of(partyId.toString())
        ));
        if (motDePasse != null) {
            corps.put("credentials", List.of(Map.of(
                    "type", "password",
                    "value", motDePasse,
                    "temporary", false
            )));
        }
        return corps;
    }
}
