package com.gateway.payment.identity.enrolement;

import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.DmnEngineConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Évalue {@code enrolement-pieces.dmn} : pays + type de document → faces à photographier. */
@Component
public class PiecesDecision implements PiecesEnrolement {

    static final String DECISION = "decision_EnrolementPieces";

    private final DmnEngine moteur;

    public PiecesDecision(EnrollmentProperties enrollment) {
        DmnEngineConfiguration configuration = DmnEngineConfiguration.createStandaloneInMemDmnEngineConfiguration();
        configuration.setDatabaseSchemaUpdate(DmnEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        this.moteur = configuration.buildDmnEngine();
        Path fichier = resoudre(enrollment.getPiecesDmn());
        try {
            moteur.getDmnRepositoryService()
                    .createDeployment()
                    .name("enrolement-pieces")
                    .addDmnBytes(fichier.getFileName().toString(), Files.readAllBytes(fichier))
                    .deploy();
        } catch (Exception e) {
            throw new IllegalStateException("La décision des pièces d'enrôlement n'a pas pu être chargée", e);
        }
    }

    @Override
    public List<ParcoursPays.Document> demander(String pays, String typeDocument) {
        String paysCode = pays == null ? "" : pays.trim().toUpperCase();
        String type = typeDocument == null ? "" : typeDocument.trim().toUpperCase();
        List<Map<String, Object>> profils = moteur.getDmnDecisionService()
                .createExecuteDecisionBuilder()
                .decisionKey("decision_EnrolementProfil")
                .variable("pays", paysCode)
                .variable("typeDocument", type)
                .execute();
        String profil = profils.isEmpty() ? "" : texte(profils.getFirst().get("profil"));
        List<Map<String, Object>> lignes = moteur.getDmnDecisionService()
                .createExecuteDecisionBuilder()
                .decisionKey(DECISION)
                .variable("pays", paysCode)
                .variable("typeDocument", type)
                .variable("profil", profil == null ? "" : profil)
                .execute();
        List<ParcoursPays.Document> documents = new ArrayList<>();
        for (Map<String, Object> ligne : lignes) {
            String face = texte(ligne.get("face"));
            if (face == null) {
                continue;
            }
            documents.add(new ParcoursPays.Document(
                    face,
                    texte(ligne.get("libelle")),
                    Boolean.TRUE.equals(ligne.get("requis")) || "true".equalsIgnoreCase(texte(ligne.get("requis")))
            ));
        }
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("Aucune pièce n'est paramétrée pour ce document");
        }
        return documents;
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

    private static String texte(Object valeur) {
        if (valeur == null) {
            return null;
        }
        String texte = valeur.toString().trim();
        return texte.isEmpty() ? null : texte;
    }
}
