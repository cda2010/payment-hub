package com.gateway.payment.identity.enrolement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lit le workflow publié et les formulaires Form.io du produit ENROLEMENT. */
@Component
public class FormulaireCatalogue {

    private final EnrollmentProperties enrollment;
    private final ObjectMapper json;

    public FormulaireCatalogue(EnrollmentProperties enrollment, ObjectMapper json) {
        this.enrollment = enrollment;
        this.json = json;
    }

    public WorkflowEnrolement publie() {
        JsonNode configuration = configuration();
        String workflow = texte(configuration, "workflow");
        if (workflow == null) {
            workflow = "enrolement";
        }
        Map<String, EcranEnrolement> partages = new LinkedHashMap<>();
        Map<String, Map<String, EcranEnrolement>> variantes = new LinkedHashMap<>();
        JsonNode bindings = configuration.get("bindings");
        if (bindings != null && bindings.isArray()) {
            for (JsonNode binding : bindings) {
                if (!"UI".equalsIgnoreCase(texte(binding, "kind"))) {
                    continue;
                }
                String formKey = texte(binding, "formKey");
                String etape = texte(binding, "step");
                if (formKey == null || etape == null) {
                    continue;
                }
                EcranEnrolement ecran = lire(etape, formKey);
                String pays = texte(binding, "providerCode");
                if (pays == null) {
                    partages.putIfAbsent(etape, ecran);
                } else {
                    variantes.computeIfAbsent(etape, cle -> new LinkedHashMap<>()).put(pays.toUpperCase(), ecran);
                }
            }
        }
        if (partages.isEmpty()) {
            throw new IllegalStateException("Le workflow d'enrôlement ne publie aucun formulaire");
        }
        return new WorkflowEnrolement(workflow, List.copyOf(partages.values()), variantes);
    }

    private JsonNode configuration() {
        String sql = """
                SELECT v.configuration::text
                FROM catalog_de_service s
                JOIN catalog_de_service_version v ON v.id = s.current_version_id
                WHERE s.code = 'ENROLEMENT' AND s.status = 'PUBLISHED' AND v.status = 'PUBLISHED'
                """;
        try (Connection connection = DriverManager.getConnection(
                        enrollment.getCatalogDbUrl(),
                        enrollment.getCatalogDbUser(),
                        enrollment.getCatalogDbPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            if (!rows.next()) {
                throw new IllegalStateException("Le produit d'enrôlement est absent du catalogue");
            }
            return json.readTree(rows.getString(1));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Le catalogue n'a pas répondu");
        }
    }

    private EcranEnrolement lire(String etape, String formKey) {
        Path fichier = Path.of(enrollment.getFormsDir(), formKey + ".json");
        if (!Files.isRegularFile(fichier)) {
            throw new IllegalStateException("Formulaire introuvable : " + formKey);
        }
        try {
            JsonNode racine = json.readTree(fichier.toFile());
            JsonNode formio = racine.get("formio");
            String titre = texte(formio, "title");
            List<EcranEnrolement.Champ> champs = new ArrayList<>();
            JsonNode components = formio == null ? null : formio.get("components");
            if (components != null && components.isArray()) {
                for (JsonNode composant : components) {
                    EcranEnrolement.Champ champ = champ(composant);
                    if (champ != null) {
                        champs.add(champ);
                    }
                }
            }
            return new EcranEnrolement(etape, formKey, titre == null ? formKey : titre, champs);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Formulaire illisible : " + formKey);
        }
    }

    private static EcranEnrolement.Champ champ(JsonNode composant) {
        String code = texte(composant, "key");
        if (code == null || "intro".equals(code)) {
            return null;
        }
        String type = switch (texte(composant, "type") == null ? "" : texte(composant, "type")) {
            case "radio", "select" -> "CHOICE";
            case "datetime", "day" -> "DATE";
            case "file" -> "FILE";
            case "phoneNumber" -> "PHONE";
            default -> "TEXT";
        };
        boolean requis = composant.path("validate").path("required").asBoolean(false);
        return new EcranEnrolement.Champ(code, texte(composant, "label"), type, requis, options(composant));
    }

    private static List<EcranEnrolement.Option> options(JsonNode composant) {
        List<EcranEnrolement.Option> options = new ArrayList<>();
        JsonNode valeurs = composant.get("values");
        if (valeurs == null && composant.get("data") != null) {
            valeurs = composant.get("data").get("values");
        }
        if (valeurs == null || !valeurs.isArray()) {
            return options;
        }
        for (JsonNode option : valeurs) {
            String libelle = texte(option, "label");
            String valeur = texte(option, "value");
            if (valeur != null) {
                options.add(new EcranEnrolement.Option(libelle == null ? valeur : libelle, valeur));
            }
        }
        return options;
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
