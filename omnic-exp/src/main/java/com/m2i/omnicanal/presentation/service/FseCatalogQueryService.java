package com.m2i.omnicanal.presentation.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lecture du paramétrage FSE pour payment-2 : fournisseur, API, méthodes, interfaces.
 */
@Service
public class FseCatalogQueryService {

    private final JdbcTemplate jdbc;

    public FseCatalogQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> catalog(String codeFse) {
        List<Map<String, Object>> fournisseurs = jdbc.queryForList("""
                SELECT f.id, f.code_fse, f.nom, f.groupe, f.code_externe, f.statut, p.nom_pays,
                       f.type_moyen, f.mode_execution
                FROM fournisseur_service f
                LEFT JOIN pays p ON p.id = f.pays_id
                WHERE f.code_fse = ?
                """, codeFse);
        if (fournisseurs.isEmpty()) {
            return Map.of("found", false, "codeFse", codeFse);
        }
        Map<String, Object> fse = fournisseurs.get(0);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("found", true);
        body.put("codeFse", fse.get("code_fse"));
        body.put("nom", fse.get("nom"));
        body.put("groupe", fse.get("groupe"));
        body.put("codeExterne", fse.get("code_externe"));
        body.put("statut", fse.get("statut"));
        body.put("pays", fse.get("nom_pays"));
        body.put("typeMoyen", fse.get("type_moyen"));
        body.put("modeExecution", fse.get("mode_execution"));
        body.put("apis", apis(codeFse));
        body.put("interfaces", interfaces(codeFse));
        return body;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> fournisseurs() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT f.code_fse, f.nom, f.groupe, f.code_externe, f.statut, p.nom_pays,
                       f.type_moyen, f.mode_execution
                FROM fournisseur_service f
                LEFT JOIN pays p ON p.id = f.pays_id
                ORDER BY f.code_fse
                """);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codeFse", row.get("code_fse"));
            item.put("nom", row.get("nom"));
            item.put("groupe", row.get("groupe"));
            item.put("codeExterne", row.get("code_externe"));
            item.put("statut", row.get("statut"));
            item.put("pays", row.get("nom_pays"));
            item.put("typeMoyen", row.get("type_moyen"));
            item.put("modeExecution", row.get("mode_execution"));
            out.add(item);
        }
        return out;
    }

    /**
     * Met à jour la ligne {@code fournisseur_service}. Une valeur absente ou nulle
     * conserve la colonne actuelle.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> updateFournisseur(String codeFse, Map<String, Object> patch) {
        String current = codeFse == null ? "" : codeFse.trim();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT f.id, f.code_fse, f.nom, f.groupe, f.code_externe, f.statut, f.pays_id,
                       f.type_moyen, f.mode_execution
                FROM fournisseur_service f
                WHERE f.code_fse = ?
                """, current);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fournisseur introuvable: " + current);
        }
        Map<String, Object> row = rows.get(0);
        String nom = required(patch.get("nom"), string(row.get("nom")), 255, "Le libellé");
        String groupe = optional(patch, "groupe", string(row.get("groupe")), 255, "Le groupe");
        String codeExterne = optional(patch, "codeExterne", string(row.get("code_externe")), 10, "Le code externe");
        String statut = required(patch.get("statut"), string(row.get("statut")), 11, "Le statut");
        if (!STATUTS.contains(statut)) {
            throw badRequest("Statut inconnu: " + statut + ". Valeurs attendues: BANQUE, NONBANQUE, AUTRE.");
        }
        String typeMoyen = optional(patch, "typeMoyen", string(row.get("type_moyen")), 30, "Le type de moyen");
        String modeExecution = optional(patch, "modeExecution", string(row.get("mode_execution")), 30, "Le mode d'exécution");
        String newCode = required(patch.get("codeFse"), current, 255, "Le code FSE");
        if (!newCode.equals(current)) {
            Integer taken = jdbc.queryForObject(
                    "SELECT count(*) FROM fournisseur_service WHERE code_fse = ?",
                    Integer.class,
                    newCode);
            if (taken != null && taken > 0) {
                throw badRequest("Code FSE déjà utilisé: " + newCode);
            }
        }
        Long paysId = row.get("pays_id") == null ? null : ((Number) row.get("pays_id")).longValue();
        String pays = patch.containsKey("pays") ? string(patch.get("pays")) : null;
        if (patch.containsKey("pays")) {
            paysId = resolvePays(pays, paysId);
        }
        jdbc.update("""
                UPDATE fournisseur_service
                SET nom = ?, groupe = ?, code_externe = ?, statut = ?,
                    type_moyen = ?, mode_execution = ?, pays_id = ?, code_fse = ?
                WHERE code_fse = ?
                """, nom, groupe, codeExterne, statut, typeMoyen, modeExecution, paysId, newCode, current);
        return fournisseur(newCode);
    }

    private Map<String, Object> fournisseur(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT f.code_fse, f.nom, f.groupe, f.code_externe, f.statut, p.nom_pays,
                       f.type_moyen, f.mode_execution
                FROM fournisseur_service f
                LEFT JOIN pays p ON p.id = f.pays_id
                WHERE f.code_fse = ?
                """, codeFse);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fournisseur introuvable: " + codeFse);
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("codeFse", row.get("code_fse"));
        item.put("nom", row.get("nom"));
        item.put("groupe", row.get("groupe"));
        item.put("codeExterne", row.get("code_externe"));
        item.put("statut", row.get("statut"));
        item.put("pays", row.get("nom_pays"));
        item.put("typeMoyen", row.get("type_moyen"));
        item.put("modeExecution", row.get("mode_execution"));
        return item;
    }

    private Long resolvePays(String pays, Long current) {
        if (pays == null) {
            return current;
        }
        List<Long> ids = jdbc.query(
                "SELECT id FROM pays WHERE lower(nom_pays) = lower(?)",
                (rs, rowNum) -> rs.getLong(1),
                pays);
        if (ids.isEmpty()) {
            throw badRequest("Pays inconnu: " + pays + ". Indiquez le nom enregistré (ex. Burkina Faso).");
        }
        return ids.get(0);
    }

    private static String required(Object incoming, String current, int max, String label) {
        String value = incoming == null ? current : string(incoming);
        if (value == null) {
            value = current;
        }
        if (value == null || value.isBlank()) {
            throw badRequest(label + " est obligatoire.");
        }
        if (value.length() > max) {
            throw badRequest(label + " dépasse " + max + " caractères.");
        }
        return value;
    }

    /** Champ effaçable : vide devient null, absent conserve la valeur actuelle. */
    private static String optional(Map<String, Object> patch, String key, String current, int max, String label) {
        if (!patch.containsKey(key) || patch.get(key) == null) {
            return fit(current, max, label);
        }
        String value = string(patch.get(key));
        return fit(value, max, label);
    }

    private static String fit(String value, int max, String label) {
        if (value == null) {
            return null;
        }
        if (value.length() > max) {
            throw badRequest(label + " dépasse " + max + " caractères.");
        }
        return value;
    }

    private static String string(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static final Set<String> STATUTS = Set.of("BANQUE", "NONBANQUE", "AUTRE");

    @Transactional(readOnly = true)
    public List<Map<String, Object>> comptes() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT c.id, c.code_banque, c.code_agence, c.numero_compte, c.cle, c.extype,
                       c.devise, c.libelle_compte, c.code_uo_titulaire, c.mirroir_id,
                       tc.code_parametre AS type_code, tc.nom_type_compte AS type_libelle,
                       ft.code_fse AS fse_teneur, fti.code_fse AS fse_titulaire,
                       ag.code_agence AS agence_code,
                       ts.code_type_service AS service_code,
                       ts.libelle_type_service AS service_libelle,
                       r.id AS relation_id,
                       fs.code_fse AS fse_source, fd.code_fse AS fse_destination,
                       rts.code_type_service AS relation_service,
                       rts.libelle_type_service AS relation_service_libelle
                FROM compte c
                JOIN type_compte tc ON tc.id = c.type_compte_id
                JOIN fournisseur_service ft ON ft.id = c.fse_teneur_id
                LEFT JOIN fournisseur_service fti ON fti.id = c.fse_titulaire_id
                JOIN agence ag ON ag.id = c.agence_id
                LEFT JOIN type_service ts ON ts.id = c.service_id
                LEFT JOIN relation r ON r.id = c.relation_id
                LEFT JOIN fournisseur_service fs ON fs.id = r.fse_source_id
                LEFT JOIN fournisseur_service fd ON fd.id = r.fse_destination_id
                LEFT JOIN type_service rts ON rts.id = r.service_id
                ORDER BY ft.code_fse, tc.code_parametre, c.numero_compte
                """);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ((Number) row.get("id")).longValue());
            item.put("codeBanque", row.get("code_banque"));
            item.put("codeAgence", row.get("code_agence"));
            item.put("numeroCompte", row.get("numero_compte"));
            item.put("cle", row.get("cle"));
            item.put("extype", row.get("extype"));
            item.put("devise", row.get("devise"));
            item.put("libelleCompte", row.get("libelle_compte"));
            item.put("codeUoTitulaire", row.get("code_uo_titulaire"));
            item.put("mirroirId", row.get("mirroir_id") == null ? null : ((Number) row.get("mirroir_id")).longValue());
            item.put("typeCompteCode", row.get("type_code"));
            item.put("typeCompteLibelle", row.get("type_libelle"));
            item.put("fseTeneur", row.get("fse_teneur"));
            item.put("fseTitulaire", row.get("fse_titulaire"));
            item.put("agenceCode", row.get("agence_code"));
            item.put("serviceCode", row.get("service_code"));
            item.put("serviceLibelle", row.get("service_libelle"));
            item.put("relationId", row.get("relation_id") == null ? null : ((Number) row.get("relation_id")).longValue());
            item.put("fseSource", row.get("fse_source"));
            item.put("fseDestination", row.get("fse_destination"));
            item.put("relationService", row.get("relation_service"));
            item.put("relationServiceLibelle", row.get("relation_service_libelle"));
            out.add(item);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> schemas() {
        Map<Long, List<Map<String, Object>>> params = schemaParams();
        Map<Long, List<Map<String, Object>>> lines = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT id, schema_ecriture_id, ordre, type_methode, api_a_utiliser, priorite,
                       agence_a_imputer, step_id
                FROM ligne_schema_ecriture
                ORDER BY schema_ecriture_id, ordre, id
                """)) {
            long schemaId = ((Number) row.get("schema_ecriture_id")).longValue();
            long id = ((Number) row.get("id")).longValue();
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("id", id);
            line.put("ordre", row.get("ordre"));
            line.put("typeMethode", row.get("type_methode"));
            line.put("apiAUtiliser", row.get("api_a_utiliser"));
            line.put("priorite", row.get("priorite"));
            line.put("agenceImputer", row.get("agence_a_imputer"));
            line.put("stepId", row.get("step_id"));
            line.put("parametres", params.getOrDefault(id, List.of()));
            lines.computeIfAbsent(schemaId, key -> new ArrayList<>()).add(line);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT id, nom_schema, pattern_code, type_service, engine, workflow_name, workflow_version
                FROM schema_ecriture
                ORDER BY nom_schema
                """)) {
            long id = ((Number) row.get("id")).longValue();
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("id", id);
            schema.put("nomSchema", row.get("nom_schema"));
            schema.put("patternCode", row.get("pattern_code"));
            schema.put("typeService", row.get("type_service"));
            schema.put("engine", row.get("engine"));
            schema.put("workflowName", row.get("workflow_name"));
            schema.put("workflowVersion", row.get("workflow_version"));
            schema.put("lignes", lines.getOrDefault(id, List.of()));
            out.add(schema);
        }
        return out;
    }

    private Map<Long, List<Map<String, Object>>> schemaParams() {
        Map<Long, List<Map<String, Object>>> byLine = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT id, ligne_schema_ecriture_id, nom_parametre, code_parametre,
                       code_parametre_methode, type_parametre, valeur, code_categorie
                FROM parametre_schema
                ORDER BY id
                """)) {
            long lineId = ((Number) row.get("ligne_schema_ecriture_id")).longValue();
            Map<String, Object> param = new LinkedHashMap<>();
            param.put("id", ((Number) row.get("id")).longValue());
            param.put("nomParametre", row.get("nom_parametre"));
            param.put("codeParametre", row.get("code_parametre"));
            param.put("codeParametreMethode", row.get("code_parametre_methode"));
            param.put("typeParametre", row.get("type_parametre"));
            param.put("valeur", row.get("valeur"));
            param.put("codeCategorie", row.get("code_categorie"));
            byLine.computeIfAbsent(lineId, key -> new ArrayList<>()).add(param);
        }
        return byLine;
    }

    private List<Map<String, Object>> apis(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT a.id, a.code_api, a.nom_api, a.type, a.host, a.port, a.use_ssl, a.use_proxy, a.actif,
                       a.type_autorization, a.client_id, a.authentification_url, a.grant_type,
                       a.client_secret, a.login, a.password
                FROM api a
                JOIN fournisseur_service f ON f.id = a.fournisseur_service_id
                WHERE f.code_fse = ?
                ORDER BY a.code_api
                """, codeFse);
        Map<Long, List<Map<String, Object>>> methods = methodsByApi(codeFse);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            Map<String, Object> api = new LinkedHashMap<>();
            api.put("id", id);
            api.put("code", row.get("code_api"));
            api.put("nom", row.get("nom_api"));
            api.put("type", row.get("type"));
            api.put("host", row.get("host"));
            api.put("port", row.get("port"));
            api.put("useSsl", flag(row.get("use_ssl")));
            api.put("useProxy", flag(row.get("use_proxy")));
            api.put("actif", row.get("actif") == null || Boolean.TRUE.equals(row.get("actif")));
            api.put("authType", row.get("type_autorization"));
            api.put("clientId", row.get("client_id"));
            api.put("tokenUrl", row.get("authentification_url"));
            api.put("grantType", row.get("grant_type"));
            api.put("hasClientSecret", text(row.get("client_secret")));
            api.put("hasUsername", text(row.get("login")));
            api.put("hasPassword", text(row.get("password")));
            api.put("methods", methods.getOrDefault(id, List.of()));
            out.add(api);
        }
        return out;
    }

    private Map<Long, List<Map<String, Object>>> methodsByApi(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT m.id, m.api_id, m.type_methode, m.code_methode, m.nom_methode, m.type_appel, m.type_reponse
                FROM methode m
                JOIN api a ON a.id = m.api_id
                JOIN fournisseur_service f ON f.id = a.fournisseur_service_id
                WHERE f.code_fse = ?
                ORDER BY m.type_methode
                """, codeFse);
        Map<Long, List<Map<String, Object>>> params = paramsByMethod(codeFse);
        Map<Long, List<Map<String, Object>>> byApi = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            long apiId = ((Number) row.get("api_id")).longValue();
            Map<String, Object> method = new LinkedHashMap<>();
            method.put("id", id);
            method.put("typeMethode", row.get("type_methode"));
            method.put("codeMethode", row.get("code_methode"));
            method.put("nomMethode", row.get("nom_methode"));
            method.put("typeAppel", row.get("type_appel"));
            method.put("typeReponse", row.get("type_reponse"));
            method.put("parametres", params.getOrDefault(id, List.of()));
            byApi.computeIfAbsent(apiId, key -> new ArrayList<>()).add(method);
        }
        return byApi;
    }

    private Map<Long, List<Map<String, Object>>> paramsByMethod(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT pm.id, pm.methode_id, pm.code_parametre, pm.nom_externe, pm.type_parametre, pm.nom_parametre
                FROM parametre_methode pm
                JOIN methode m ON m.id = pm.methode_id
                JOIN api a ON a.id = m.api_id
                JOIN fournisseur_service f ON f.id = a.fournisseur_service_id
                WHERE f.code_fse = ?
                ORDER BY pm.id
                """, codeFse);
        Map<Long, List<Map<String, Object>>> byMethod = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            long methodId = ((Number) row.get("methode_id")).longValue();
            Map<String, Object> param = new LinkedHashMap<>();
            param.put("id", ((Number) row.get("id")).longValue());
            param.put("codeParametre", row.get("code_parametre"));
            param.put("nomExterne", row.get("nom_externe"));
            param.put("typeParametre", row.get("type_parametre"));
            param.put("nomParametre", row.get("nom_parametre"));
            byMethod.computeIfAbsent(methodId, key -> new ArrayList<>()).add(param);
        }
        return byMethod;
    }

    private List<Map<String, Object>> interfaces(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT i.id, i.code_interface, i.nom_interface, i.type_methode, i.type_interface,
                       m.code_methode, m.nom_methode
                FROM interface_utilisateur i
                JOIN fournisseur_service f ON f.id = i.fournisseur_service_id
                LEFT JOIN methode m ON m.id = i.methode_id
                WHERE f.code_fse = ?
                ORDER BY i.type_methode
                """, codeFse);
        Map<Long, List<Map<String, Object>>> params = interfaceParams(codeFse);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", id);
            item.put("codeInterface", row.get("code_interface"));
            item.put("nomInterface", row.get("nom_interface"));
            item.put("typeMethode", row.get("type_methode"));
            item.put("typeEnvoi", row.get("type_interface"));
            item.put("methodeCode", row.get("code_methode"));
            item.put("methodeNom", row.get("nom_methode"));
            item.put("parametres", params.getOrDefault(id, List.of()));
            out.add(item);
        }
        return out;
    }

    private Map<Long, List<Map<String, Object>>> interfaceParams(String codeFse) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT pu.id, pu.interface_utilisateur_id, pu.code_parametre, pu.nom_parametre,
                       pu.type_parametre, pu.valeur, pu.code_parametre_methode, pu.attribut
                FROM parametre_utilisateur pu
                JOIN interface_utilisateur i ON i.id = pu.interface_utilisateur_id
                JOIN fournisseur_service f ON f.id = i.fournisseur_service_id
                WHERE f.code_fse = ?
                ORDER BY pu.id
                """, codeFse);
        Map<Long, List<Map<String, Object>>> byInterface = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            long interfaceId = ((Number) row.get("interface_utilisateur_id")).longValue();
            Map<String, Object> param = new LinkedHashMap<>();
            param.put("id", ((Number) row.get("id")).longValue());
            param.put("codeParametre", row.get("code_parametre"));
            param.put("nomParametre", row.get("nom_parametre"));
            param.put("typeParametre", row.get("type_parametre"));
            param.put("valeur", row.get("valeur"));
            param.put("codeParametreMethode", row.get("code_parametre_methode"));
            param.put("attribut", row.get("attribut"));
            byInterface.computeIfAbsent(interfaceId, key -> new ArrayList<>()).add(param);
        }
        return byInterface;
    }

    private static boolean flag(Object raw) {
        if (raw instanceof Number number) {
            return number.intValue() != 0;
        }
        if (raw instanceof Boolean bool) {
            return bool;
        }
        return false;
    }

    private static boolean text(Object raw) {
        return raw != null && !String.valueOf(raw).isBlank();
    }
}
