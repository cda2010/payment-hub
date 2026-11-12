package com.gateway.payment.identity.enrolement;

import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.DmnEngineConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Évalue {@code enrolement-profil-limite.dmn} : segment + choix → code du profil de limite. */
@Component
public class ProfilLimiteDecision {

    static final String DECISION = "decision_EnrolementProfilLimite";

    private final DmnEngine moteur;

    public ProfilLimiteDecision(EnrollmentProperties enrollment) {
        DmnEngineConfiguration configuration = DmnEngineConfiguration.createStandaloneInMemDmnEngineConfiguration();
        configuration.setDatabaseSchemaUpdate(DmnEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        this.moteur = configuration.buildDmnEngine();
        Path fichier = resoudre(enrollment.getProfilLimiteDmn());
        try {
            moteur.getDmnRepositoryService()
                    .createDeployment()
                    .name("enrolement-profil-limite")
                    .addDmnBytes(fichier.getFileName().toString(), Files.readAllBytes(fichier))
                    .deploy();
        } catch (Exception e) {
            throw new IllegalStateException("La décision du profil de limite n'a pas pu être chargée", e);
        }
    }

    public String profil(String segment, String choix) {
        List<Map<String, Object>> lignes = moteur.getDmnDecisionService()
                .createExecuteDecisionBuilder()
                .decisionKey(DECISION)
                .variable("segment", segment == null ? "" : segment.trim().toUpperCase())
                .variable("choix", choix == null ? "" : choix.trim().toUpperCase())
                .execute();
        if (lignes.isEmpty() || lignes.getFirst().get("profil") == null) {
            throw new IllegalArgumentException("Aucun profil de limite n'est paramétré pour ce choix");
        }
        return lignes.getFirst().get("profil").toString().trim();
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
}
