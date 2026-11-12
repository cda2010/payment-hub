package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CatalogueGateway implements CatalogueEnrolement {

    private final EnrollmentProperties enrollment;
    private final ObjectMapper json;

    private volatile Map<String, ParcoursPays> parcours;
    private volatile String defaut;

    public CatalogueGateway(EnrollmentProperties enrollment, ObjectMapper json) {
        this.enrollment = enrollment;
        this.json = json;
    }

    @Override
    public String defautPays() {
        charger();
        return defaut;
    }

    @Override
    public List<ParcoursPays> pays() {
        charger();
        return List.copyOf(parcours.values());
    }

    @Override
    public ParcoursPays pour(String code) {
        charger();
        String cle = code == null || code.isBlank() ? defaut : code.trim().toUpperCase();
        ParcoursPays trouve = parcours.get(cle);
        if (trouve == null) {
            throw new IllegalArgumentException("Ce pays n'ouvre pas le produit d'enrôlement");
        }
        return trouve;
    }

    private void charger() {
        if (parcours != null) {
            return;
        }
        synchronized (this) {
            if (parcours != null) {
                return;
            }
            JsonNode configuration = configurationPubliee();
            JsonNode pays = configuration.get("pays");
            if (pays == null || !pays.isObject() || pays.isEmpty()) {
                throw new IllegalStateException("Le produit d'enrôlement ne publie aucun pays");
            }
            Map<String, ParcoursPays> lus = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> champs = pays.fields();
            while (champs.hasNext()) {
                Map.Entry<String, JsonNode> entree = champs.next();
                lus.put(entree.getKey().toUpperCase(), lire(entree.getKey().toUpperCase(), entree.getValue()));
            }
            String choisi = texte(configuration, "defautPays");
            this.defaut = choisi != null && lus.containsKey(choisi.toUpperCase())
                    ? choisi.toUpperCase()
                    : lus.keySet().iterator().next();
            this.parcours = lus;
        }
    }

    private JsonNode configurationPubliee() {
        String url = enrollment.getCatalogDbUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Le catalogue n'est pas configuré");
        }
        String sql = """
                SELECT v.configuration::text
                FROM catalog_de_service s
                JOIN catalog_de_service_version v ON v.id = s.current_version_id
                WHERE s.code = ? AND s.status = 'PUBLISHED' AND v.status = 'PUBLISHED'
                """;
        try (Connection connection = DriverManager.getConnection(
                        url, enrollment.getCatalogDbUser(), enrollment.getCatalogDbPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, CODE);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Le produit d'enrôlement est absent du catalogue");
                }
                return json.readTree(rows.getString(1));
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le catalogue n'a pas répondu");
        }
    }

    private ParcoursPays lire(String code, JsonNode noeud) {
        JsonNode ouverture = noeud.get("ouverture");
        return new ParcoursPays(
                code,
                texte(noeud, "nom"),
                texte(noeud, "devise"),
                textes(noeud.get("maisons")),
                champs(ouverture == null ? null : ouverture.get("champs")),
                documents(ouverture == null ? null : ouverture.get("documents"))
        );
    }

    private static List<ParcoursPays.Champ> champs(JsonNode noeud) {
        List<ParcoursPays.Champ> liste = new ArrayList<>();
        if (noeud == null || !noeud.isArray()) {
            return liste;
        }
        for (JsonNode champ : noeud) {
            String code = texte(champ, "code");
            if (code == null) {
                continue;
            }
            liste.add(new ParcoursPays.Champ(
                    code,
                    texte(champ, "libelle"),
                    texte(champ, "type") == null ? "TEXT" : texte(champ, "type"),
                    champ.path("requis").asBoolean(false)
            ));
        }
        return liste;
    }

    private static List<ParcoursPays.Document> documents(JsonNode noeud) {
        List<ParcoursPays.Document> liste = new ArrayList<>();
        if (noeud == null || !noeud.isArray()) {
            return liste;
        }
        for (JsonNode document : noeud) {
            String face = texte(document, "face");
            if (face == null) {
                continue;
            }
            liste.add(new ParcoursPays.Document(
                    face.toUpperCase(),
                    texte(document, "libelle"),
                    document.path("requis").asBoolean(true)
            ));
        }
        return liste;
    }

    private static List<String> textes(JsonNode noeud) {
        List<String> liste = new ArrayList<>();
        if (noeud == null || !noeud.isArray()) {
            return liste;
        }
        for (JsonNode valeur : noeud) {
            if (!valeur.isNull()) {
                liste.add(valeur.asText().trim().toUpperCase());
            }
        }
        return liste;
    }

    private static String texte(JsonNode noeud, String champ) {
        JsonNode valeur = noeud == null ? null : noeud.get(champ);
        if (valeur == null || valeur.isNull()) {
            return null;
        }
        String texte = valeur.asText().trim();
        return texte.isEmpty() ? null : texte;
    }

}
