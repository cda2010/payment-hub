package com.gateway.payment.network.connexion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gateway.payment.network.NetworkException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Crée l'utilisateur Keycloak sur le login personnel. Le mot de passe est temporaire. */
@Component
public class HttpConnexionGuichet implements ConnexionGuichet {

    private final String keycloakUrl;
    private final String realm;
    private final String adminUser;
    private final String adminPassword;
    private final ObjectMapper json;
    private final RestClient http;

    public HttpConnexionGuichet(
            @Value("${network.connexion.keycloak-url:http://127.0.0.1:8180}") String keycloakUrl,
            @Value("${network.connexion.realm:payment}") String realm,
            @Value("${network.connexion.admin-user:admin}") String adminUser,
            @Value("${network.connexion.admin-password:admin}") String adminPassword,
            ObjectMapper json
    ) {
        this.keycloakUrl = keycloakUrl.replaceAll("/+$", "");
        this.realm = realm;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public void ouvrir(
            UUID utilisateurId,
            UUID uniteId,
            String identifiant,
            String nom,
            String roleMetier,
            String audience,
            String motDePasse,
            String email
    ) {
        try {
            String jeton = jetonAdmin();
            String utilisateur = assurerUtilisateur(
                    jeton, utilisateurId, uniteId, identifiant, nom, audience, motDePasse, email);
            assurerRole(jeton, utilisateur, roleConnexion(roleMetier));
        } catch (NetworkException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new NetworkException(HttpStatus.BAD_GATEWAY, "CONNEXION_INDISPONIBLE",
                    "Le compte de connexion n'a pas pu être ouvert.");
        }
    }

    @Override
    public void definirAcces(String identifiant, boolean actif) {
        try {
            String jeton = jetonAdmin();
            String id = chercher(jeton, identifiant);
            String corps = http.get()
                    .uri(admin("/users/" + id))
                    .header("Authorization", "Bearer " + jeton)
                    .retrieve()
                    .body(String.class);
            JsonNode noeud = json.readTree(corps);
            if (!(noeud instanceof ObjectNode objet)) {
                throw indisponible();
            }
            objet.put("enabled", actif);
            http.put()
                    .uri(admin("/users/" + id))
                    .header("Authorization", "Bearer " + jeton)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objet)
                    .retrieve()
                    .toBodilessEntity();
            if (!actif) {
                try {
                    http.post()
                            .uri(admin("/users/" + id + "/logout"))
                            .header("Authorization", "Bearer " + jeton)
                            .retrieve()
                            .toBodilessEntity();
                } catch (RuntimeException ignored) {
                    // Le compte est déjà fermé : une session résiduelle expire avec le jeton.
                }
            }
        } catch (NetworkException e) {
            throw e;
        } catch (Exception e) {
            throw indisponible();
        }
    }

    @Override
    public void rattacher(String identifiant, String roleMetier) {
        try {
            String jeton = jetonAdmin();
            String id = chercher(jeton, identifiant);
            assurerRole(jeton, id, roleConnexion(roleMetier));
        } catch (NetworkException e) {
            throw e;
        } catch (RuntimeException e) {
            throw indisponible();
        }
    }

    @Override
    public void reinitialiserMotDePasse(String identifiant, String motDePasse) {
        try {
            String jeton = jetonAdmin();
            String id = chercher(jeton, identifiant);
            http.put()
                    .uri(admin("/users/" + id + "/reset-password"))
                    .header("Authorization", "Bearer " + jeton)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("type", "password", "value", motDePasse, "temporary", true))
                    .retrieve()
                    .toBodilessEntity();
        } catch (NetworkException e) {
            throw e;
        } catch (RuntimeException e) {
            throw indisponible();
        }
    }

    private String assurerUtilisateur(
            String jeton,
            UUID utilisateurId,
            UUID uniteId,
            String identifiant,
            String nom,
            String audience,
            String motDePasse,
            String email
    ) {
        Map<String, Object> corps = representation(utilisateurId, uniteId, identifiant, nom, audience, motDePasse, email);
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
                throw indisponible();
            }
            return emplacement.substring(emplacement.lastIndexOf('/') + 1);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 409) {
                throw indisponible();
            }
        }
        String id = chercher(jeton, identifiant);
        if (!utilisateurId.toString().equals(utilisateurRattache(jeton, id))) {
            throw NetworkException.conflit("UTILISATEUR_EXISTANT", "Ce login est déjà utilisé.");
        }
        http.put()
                .uri(admin("/users/" + id + "/reset-password"))
                .header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "password", "value", motDePasse, "temporary", true))
                .retrieve()
                .toBodilessEntity();
        return id;
    }

    private String utilisateurRattache(String jeton, String id) {
        try {
            String corps = http.get()
                    .uri(admin("/users/" + id))
                    .header("Authorization", "Bearer " + jeton)
                    .retrieve()
                    .body(String.class);
            JsonNode attributs = json.readTree(corps).path("attributes").path("utilisateur_id");
            if (attributs.isArray() && !attributs.isEmpty()) {
                return attributs.get(0).asText("");
            }
            return "";
        } catch (Exception e) {
            throw indisponible();
        }
    }

    private void assurerRole(String jeton, String utilisateur, String role) {
        String corps = http.get()
                .uri(admin("/roles/" + role))
                .header("Authorization", "Bearer " + jeton)
                .retrieve()
                .body(String.class);
        try {
            JsonNode noeud = json.readTree(corps);
            http.post()
                    .uri(admin("/users/" + utilisateur + "/role-mappings/realm"))
                    .header("Authorization", "Bearer " + jeton)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(Map.of("id", noeud.get("id").asText(), "name", role)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                return;
            }
            throw indisponible();
        } catch (Exception e) {
            throw indisponible();
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
                throw indisponible();
            }
            return liste.get(0).get("id").asText();
        } catch (NetworkException e) {
            throw e;
        } catch (Exception e) {
            throw indisponible();
        }
    }

    private String jetonAdmin() {
        LinkedMultiValueMap<String, String> formulaire = new LinkedMultiValueMap<>();
        formulaire.add("client_id", "admin-cli");
        formulaire.add("grant_type", "password");
        formulaire.add("username", adminUser);
        formulaire.add("password", adminPassword);
        try {
            String corps = http.post()
                    .uri(keycloakUrl + "/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulaire)
                    .retrieve()
                    .body(String.class);
            return json.readTree(corps).get("access_token").asText();
        } catch (Exception e) {
            throw indisponible();
        }
    }

    private String admin(String chemin) {
        return keycloakUrl + "/admin/realms/" + realm + chemin;
    }

    static String roleConnexion(String roleMetier) {
        if ("ADMIN".equals(roleMetier)) {
            return "OWNER";
        }
        if ("CAISSIER".equals(roleMetier)) {
            return "CASHIER";
        }
        return "MANAGER";
    }

    private static Map<String, Object> representation(
            UUID utilisateurId,
            UUID uniteId,
            String identifiant,
            String nom,
            String audience,
            String motDePasse,
            String email
    ) {
        Map<String, Object> corps = new LinkedHashMap<>();
        String[] parties = nom.trim().split("\\s+", 2);
        String adresse = email == null || email.isBlank() ? identifiant + "@unite.coris.local" : email;
        corps.put("username", identifiant);
        corps.put("enabled", true);
        corps.put("emailVerified", true);
        corps.put("firstName", parties[0]);
        corps.put("lastName", parties.length > 1 ? parties[1] : parties[0]);
        corps.put("email", adresse);
        corps.put("requiredActions", List.of("UPDATE_PASSWORD"));
        corps.put("attributes", Map.of(
                "audience", List.of(audience),
                "tenant_id", List.of("CORIS_DEMO"),
                "unite_id", List.of(uniteId.toString()),
                "utilisateur_id", List.of(utilisateurId.toString())
        ));
        corps.put("credentials", List.of(Map.of(
                "type", "password",
                "value", motDePasse,
                "temporary", true
        )));
        return corps;
    }

    private static NetworkException indisponible() {
        return new NetworkException(HttpStatus.BAD_GATEWAY, "CONNEXION_INDISPONIBLE",
                "Le compte de connexion n'a pas pu être ouvert.");
    }
}
