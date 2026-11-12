package com.gateway.payment.identity.enrolement;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** FSE et indicatifs lus dans les bases réseau et référentiel, pour l'enrôlement public. */
@Component
public class AnnuaireFse {

    private final EnrollmentProperties enrollment;

    public AnnuaireFse(EnrollmentProperties enrollment) {
        this.enrollment = enrollment;
    }

    public List<Operateur> operateurs(String famille, String pays) {
        String demandee = familleDemandee(famille);
        String codePays = pays == null ? "" : pays.trim().toUpperCase();
        boolean parPays = !codePays.isBlank();
        String sql = parPays ? """
                SELECT u.code, u.libelle, u.code_fse
                FROM unite_organisation u
                JOIN unite_organisation p ON p.type = 'PAYS' AND p.code = ?
                WHERE u.type = 'FSE' AND u.statut = 'ACTIF' AND u.famille_fse = ?
                  AND u.code_fse IS NOT NULL AND u.chemin LIKE p.chemin || '%'
                ORDER BY u.libelle
                """ : """
                SELECT code, libelle, code_fse
                FROM unite_organisation
                WHERE type = 'FSE' AND statut = 'ACTIF' AND famille_fse = ? AND code_fse IS NOT NULL
                ORDER BY libelle
                """;
        List<Operateur> lus = new ArrayList<>();
        try (Connection connection = reseau();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parPays) {
                statement.setString(1, codePays);
                statement.setString(2, demandee);
            } else {
                statement.setString(1, demandee);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    lus.add(new Operateur(rows.getString(1), rows.getString(2), rows.getString(3), demandee));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("La liste des FSE est indisponible");
        }
        return lus;
    }

    public boolean connue(String codeFse, String famille, String pays) {
        return operateurs(famille, pays).stream().anyMatch(operateur -> operateur.codeFse().equals(codeFse));
    }

    public List<Indicatif> indicatifs() {
        String sql = """
                SELECT code, nom, indicatif
                FROM pays
                WHERE actif = true AND indicatif IS NOT NULL AND indicatif <> ''
                ORDER BY ordre, nom
                """;
        List<Indicatif> lus = new ArrayList<>();
        try (Connection connection = referentiel();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                lus.add(new Indicatif(rows.getString("code"), rows.getString("nom"), rows.getString("indicatif")));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Les indicatifs pays sont indisponibles");
        }
        return lus;
    }

    private Connection reseau() throws Exception {
        return DriverManager.getConnection(
                enrollment.getNetworkDbUrl(), enrollment.getNetworkDbUser(), enrollment.getNetworkDbPassword());
    }

    private Connection referentiel() throws Exception {
        return DriverManager.getConnection(
                enrollment.getCatalogDbUrl(), enrollment.getCatalogDbUser(), enrollment.getCatalogDbPassword());
    }

    private static String familleDemandee(String famille) {
        if (famille == null) {
            throw new IllegalArgumentException("La famille de FSE est requise");
        }
        String demandee = famille.trim().toUpperCase();
        if (!demandee.equals("MOBILE_MONEY") && !demandee.equals("BANQUE")) {
            throw new IllegalArgumentException("La famille de FSE est requise");
        }
        return demandee;
    }

    public record Operateur(String code, String libelle, String codeFse, String famille) {
    }

    public record Indicatif(String code, String nom, String indicatif) {
    }
}
