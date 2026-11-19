package com.gateway.payment.identity.enrolement;

import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.DmnEngineConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Évalue {@code enrolement-profil-offre.dmn} : segment + choix → profil service et profil frais. */
@Component
public class ProfilOffreDecision {

    static final String DECISION = "decision_EnrolementProfilOffre";

    private final DmnEngine moteur;

    public ProfilOffreDecision(EnrollmentProperties enrollment) {
        DmnEngineConfiguration configuration = DmnEngineConfiguration.createStandaloneInMemDmnEngineConfiguration();
        configuration.setDatabaseSchemaUpdate(DmnEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        this.moteur = configuration.buildDmnEngine();
        Path fichier = resoudre(enrollment.getProfilOffreDmn());
        try {
            moteur.getDmnRepositoryService()
                    .createDeployment()
                    .name("enrolement-profil-offre")
                    .addDmnBytes(fichier.getFileName().toString(), Files.readAllBytes(fichier))
                    .deploy();
        } catch (Exception e) {
            throw new IllegalStateException("La décision des profils service et frais n'a pas pu être chargée", e);
        }
    }

    public Offre offre(String segment, String choix) {
        List<Map<String, Object>> lignes = moteur.getDmnDecisionService()
                .createExecuteDecisionBuilder()
                .decisionKey(DECISION)
                .variable("segment", segment == null ? "" : segment.trim().toUpperCase())
                .variable("choix", choix == null ? "" : choix.trim().toUpperCase())
                .execute();
        if (lignes.isEmpty()) {
            throw new IllegalArgumentException("Aucun profil service ni frais n'est paramétré pour ce choix");
        }
        Map<String, Object> ligne = lignes.getFirst();
        return new Offre(texte(ligne.get("service")), texte(ligne.get("frais")));
    }

    private static String texte(Object valeur) {
        return valeur == null ? "" : valeur.toString().trim();
    }

    private static Path resoudre(String configure) {
        Path direct = Path.of(configure);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        Path courant = Path.of("").toAbsolutePath();
        for (int niveau = 0; niveau < 4 && courant != null; niveau++) {
            Path candidat = courant.resolve(configure);
            if (Files.isRegularFile(candidat)) {
                return candidat;
            }
            courant = courant.getParent();
        }
        throw new IllegalStateException("Décision DMN introuvable : " + configure);
    }

    public record Offre(String service, String frais) {
    }
}
