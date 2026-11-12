package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Appelle l'omnicanal déjà exposé par la passerelle : GETINFOS (téléphone + indicatif)
 * ou GETINFOSCOMPTE (alias de compte dans un FSE).
 */
@Component
public class InfosOmnicanal {

    private static final Logger log = LoggerFactory.getLogger(InfosOmnicanal.class);

    private final EnrollmentProperties enrollment;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public InfosOmnicanal(EnrollmentProperties enrollment, ObjectMapper json) {
        this.enrollment = enrollment;
        this.json = json;
    }

    public Optional<Reconnaissance> getInfos(String codeFse, String indicatif, String telephone) {
        String parametres = "telephone=" + telephone + "|code=" + indicatif;
        return appeler(codeFse, "GETINFOS", parametres, telephone);
    }

    public Optional<Reconnaissance> getInfosCompte(String codeFse, String numero) {
        return appeler(codeFse, "GETINFOSCOMPTE", "alias=" + numero, numero);
    }

    private Optional<Reconnaissance> appeler(String codeFse, String methode, String parametres, String numero) {
        String base = enrollment.getOmnicanalUrl();
        if (base == null || base.isBlank()) {
            return Optional.empty();
        }
        String uri = base.replaceAll("/+$", "")
                + "/api/v1/external-omnicanal/boundary?codeFSE=" + encoder(codeFse)
                + "&codeMethode=" + methode
                + "&otrParametre=" + encoder(parametres);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                    .timeout(Duration.ofSeconds(4))
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
                log.info("omnicanal {} / {} a répondu {}", codeFse, methode, response.statusCode());
                return Optional.empty();
            }
            return lire(json.readTree(response.body()), numero);
        } catch (Exception ex) {
            log.info("omnicanal {} / {} indisponible: {}", codeFse, methode, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Reconnaissance> lire(JsonNode corps, String numero) {
        String nom = texte(corps, "nom", "nomClient", "customerName", "name");
        if (nom == null) {
            return Optional.empty();
        }
        return Optional.of(new Reconnaissance(nom, texte(corps, "prenom", "prenomClient"), numero));
    }

    private static String texte(JsonNode noeud, String... cles) {
        if (noeud == null) {
            return null;
        }
        if (noeud.isObject()) {
            for (String cle : cles) {
                JsonNode valeur = noeud.get(cle);
                if (valeur != null && valeur.isTextual() && !valeur.asText().isBlank()) {
                    return valeur.asText().trim();
                }
            }
            var champs = noeud.fields();
            while (champs.hasNext()) {
                String trouve = texte(champs.next().getValue(), cles);
                if (trouve != null) {
                    return trouve;
                }
            }
        }
        if (noeud.isArray()) {
            for (JsonNode enfant : noeud) {
                String trouve = texte(enfant, cles);
                if (trouve != null) {
                    return trouve;
                }
            }
        }
        return null;
    }

    private static String encoder(String valeur) {
        return URLEncoder.encode(valeur == null ? "" : valeur, StandardCharsets.UTF_8);
    }

    public record Reconnaissance(String nom, String prenom, String numero) {
    }
}
