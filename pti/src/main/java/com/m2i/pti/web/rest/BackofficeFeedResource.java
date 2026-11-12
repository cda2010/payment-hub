package com.m2i.pti.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lecture pour le backoffice Angular. Les listes restent vides si la base n'est pas là.
 */
@RestController
@RequestMapping("/api/backoffice")
public class BackofficeFeedResource {

    private static final Logger log = LoggerFactory.getLogger(BackofficeFeedResource.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final JdbcTemplate jdbc;

    public BackofficeFeedResource(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/clients")
    public List<Map<String, Object>> clients() {
        try {
            return jdbc.query("""
                    SELECT c.id,
                           COALESCE(pp.prenom_client, '') AS prenom,
                           COALESCE(pp.nom_client, pm.raison_sociale, c.code_client, '') AS nom,
                           COALESCE(p.telephone, '') AS telephone,
                           COALESCE(c.client_valide, false) AS valide
                    FROM client c
                    LEFT JOIN personne p ON p.id = c.personne_id
                    LEFT JOIN personne_physique pp ON pp.id = p.personne_physique_id
                    LEFT JOIN personne_morale pm ON pm.id = p.personne_morale_id
                    WHERE c.supprime IS NULL OR c.supprime = false
                    ORDER BY c.id DESC
                    LIMIT 40
                    """, (rs, row) -> client(rs));
        } catch (RuntimeException ex) {
            log.warn("Lecture clients backoffice impossible: {}", ex.getMessage());
            return List.of();
        }
    }

    @GetMapping("/operations")
    public List<Map<String, Object>> operations() {
        try {
            return jdbc.query("""
                    SELECT op.code_operation, op.montant_op, op.date_operation, op.statut, ts.code AS code_service
                    FROM operation op
                    LEFT JOIN type_service ts ON ts.id = op.type_service_id
                    ORDER BY op.date_operation DESC NULLS LAST
                    LIMIT 40
                    """, (rs, row) -> operation(rs));
        } catch (RuntimeException ex) {
            log.warn("Lecture operations backoffice impossible: {}", ex.getMessage());
            return List.of();
        }
    }

    @GetMapping("/agents")
    public List<Map<String, Object>> agents() {
        try {
            return jdbc.query("""
                    SELECT pv.code_point_vente, pv.libelle_point_vente,
                           COALESCE(u.libelle_unite_organisation, '') AS ville
                    FROM point_vente pv
                    LEFT JOIN unite_organisation u ON u.id = pv.unite_organisation_id
                    ORDER BY pv.id DESC
                    LIMIT 40
                    """, (rs, row) -> agent(rs));
        } catch (RuntimeException ex) {
            log.warn("Lecture agents backoffice impossible: {}", ex.getMessage());
            return List.of();
        }
    }

    private static Map<String, Object> client(ResultSet rs) throws SQLException {
        String prenom = rs.getString("prenom");
        String nom = rs.getString("nom");
        String name = (prenom + " " + nom).trim();
        if (name.isEmpty()) {
            name = "Client " + rs.getLong("id");
        }
        boolean valide = rs.getBoolean("valide");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", Long.toString(rs.getLong("id")));
        row.put("initials", initials(name));
        row.put("name", name);
        row.put("phone", rs.getString("telephone"));
        row.put("city", "");
        row.put("balance", "—");
        row.put("kyc", valide ? "Niveau 2" : "À traiter");
        row.put("sev", valide ? "ok" : "warn");
        row.put("since", "PTI");
        row.put("ops", "—");
        row.put("volume", "—");
        row.put("filter", valide ? "Niveau 2" : "À traiter");
        return row;
    }

    private static Map<String, Object> operation(ResultSet rs) throws SQLException {
        String code = rs.getString("code_service");
        String statut = rs.getString("statut");
        String kind = kind(code, statut);
        BigDecimal amount = rs.getBigDecimal("montant_op");
        Timestamp date = rs.getTimestamp("date_operation");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("code_operation") == null ? "" : rs.getString("code_operation"));
        row.put("title", code == null ? "Opération" : code);
        row.put("channel", "PTI");
        row.put("agent", "—");
        row.put("date", date == null ? "" : date.toLocalDateTime().format(DATE));
        row.put("amount", amount == null ? "—" : amount.toPlainString() + " F");
        row.put("status", statut == null ? "" : statut);
        row.put("sev", sev(statut));
        row.put("kind", kind);
        return row;
    }

    private static Map<String, Object> agent(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", rs.getString("libelle_point_vente"));
        row.put("code", rs.getString("code_point_vente"));
        row.put("city", rs.getString("ville"));
        row.put("float", "—");
        row.put("ops", "—");
        row.put("status", "Actif");
        row.put("sev", "ok");
        return row;
    }

    private static String initials(String name) {
        String[] parts = name.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                out.append(Character.toUpperCase(part.charAt(0)));
            }
            if (out.length() == 2) {
                break;
            }
        }
        return out.isEmpty() ? "PT" : out.toString();
    }

    private static String kind(String code, String statut) {
        String value = ((code == null ? "" : code) + " " + (statut == null ? "" : statut)).toUpperCase(Locale.ROOT);
        if (value.contains("ECHEC") || value.contains("REJET") || value.contains("ANNUL")) {
            return "Échecs";
        }
        if (value.contains("RETRAIT")) {
            return "Retraits";
        }
        if (value.contains("DEPOT") || value.contains("APPRO")) {
            return "Dépôts";
        }
        if (value.contains("FACT")) {
            return "Facturier";
        }
        return "Transferts";
    }

    private static String sev(String statut) {
        String value = statut == null ? "" : statut.toUpperCase(Locale.ROOT);
        if (value.contains("PAYE") || value.contains("VALID") || value.contains("TERMIN")) {
            return "ok";
        }
        if (value.contains("ECHEC") || value.contains("REJET") || value.contains("ANNUL")) {
            return "ko";
        }
        if (value.contains("ATTENTE")) {
            return "warn";
        }
        return "info";
    }
}
