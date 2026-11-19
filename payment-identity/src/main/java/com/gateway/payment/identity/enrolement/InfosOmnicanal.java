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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demande la fiche au moteur de la passerelle (GETINFOS ou GETINFOSCOMPTE).
 * Le moteur appelle l'omnicanal. Cette classe ne le contacte pas.
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
                + "/api/v1/boundaryByTypeMethode?codeFSE=" + encoder(codeFse)
                + "&codeMethode=" + methode
                + "&otrParametre=" + encoder(parametres);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .header("X-Api-Key", enrollment.getCanalApiKey())
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
        if (corps == null || refuse(corps)) {
            return Optional.empty();
        }
        String xml = texte(corps, "text");
        Map<String, String> champs = xml != null && xml.contains("<COMMAND") ? champsXml(xml) : new LinkedHashMap<>();
        String nom = champs.get("nom");
        String prenom = champs.get("prenom");
        if (nom == null) {
            nom = texte(corps, "nom", "nomClient", "customerName", "name");
            prenom = texte(corps, "prenom", "prenomClient");
            if (nom != null) {
                champs.put("nom", nom);
                if (prenom != null) {
                    champs.put("prenom", prenom);
                }
            }
        }
        if (nom == null) {
            return Optional.empty();
        }
        return Optional.of(new Reconnaissance(nom, prenom, numero, champs));
    }

    private static Map<String, String> champsXml(String xml) {
        Map<String, String> champs = new LinkedHashMap<>();
        poser(champs, "nom", balise(xml, "nom"));
        poser(champs, "prenom", balise(xml, "prenom"));
        poser(champs, "sexe", sexe(balise(xml, "sexe")));
        poser(champs, "dateNaissance", date(balise(xml, "dateNaissance")));
        poser(champs, "lieuNaissance", titre(balise(xml, "lieuNaissance")));
        String piece = balise(xml, "piece");
        String reference = balise(xml, "reference");
        if (piece != null || reference != null) {
            poser(champs, "piece", ((piece == null ? "" : piece) + " " + (reference == null ? "" : reference)).trim());
        }
        String telephone = balise(xml, "telephone");
        String indicatif = balise(xml, "codeIndicatif");
        if (telephone != null) {
            poser(champs, "telephone", indicatif == null ? telephone : "+" + indicatif + " " + telephone);
        }
        poser(champs, "compte", balise(xml, "numeroCompte"));
        poser(champs, "email", balise(xml, "email"));
        poser(champs, "adresse", balise(xml, "adresse"));
        return champs;
    }

    private static void poser(Map<String, String> champs, String cle, String valeur) {
        if (valeur != null && !valeur.isBlank()) {
            champs.put(cle, valeur.trim());
        }
    }

    private static String sexe(String valeur) {
        if (valeur == null) {
            return null;
        }
        return switch (valeur.trim().toUpperCase()) {
            case "M", "MASCULIN", "HOMME" -> "Masculin";
            case "F", "FEMININ", "FÉMININ", "FEMME" -> "Féminin";
            default -> titre(valeur);
        };
    }

    private static String date(String valeur) {
        if (valeur == null) {
            return null;
        }
        Matcher trouve = Pattern.compile("(\\d{2})-(\\d{2})-(\\d{4})").matcher(valeur.trim());
        return trouve.matches() ? trouve.group(1) + "/" + trouve.group(2) + "/" + trouve.group(3) : valeur.trim();
    }

    private static String titre(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        String texte = valeur.trim().toLowerCase();
        return Character.toUpperCase(texte.charAt(0)) + texte.substring(1);
    }

    /** codeErr différent de 0, ou un message « client inexistant » : le compte n'est pas reconnu. */
    private static boolean refuse(JsonNode corps) {
        String code = texte(corps, "codeErr");
        if (code != null && !"0".equals(code)) {
            return true;
        }
        JsonNode entite = corps.get("responseEntity");
        String message = entite == null ? null : texte(entite, "msg");
        return message != null && message.toLowerCase().contains("inexistant");
    }

    private static String balise(String xml, String nom) {
        Matcher trouve = Pattern.compile("<" + Pattern.quote(nom) + ">([^<]*)</" + Pattern.quote(nom) + ">").matcher(xml);
        if (!trouve.find()) {
            return null;
        }
        String valeur = trouve.group(1).trim();
        return valeur.isEmpty() ? null : valeur;
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

    public record Reconnaissance(String nom, String prenom, String numero, Map<String, String> champs) {
    }
}
