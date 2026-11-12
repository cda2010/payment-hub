package com.gateway.payment.identity.enrolement;

import java.util.List;

/** Formulaire d'ouverture Coris Money publié pour un pays du produit ENROLEMENT. */
public record ParcoursPays(
        String code,
        String nom,
        String devise,
        List<String> maisons,
        List<Champ> champs,
        List<Document> documents
) {
    public record Champ(String code, String libelle, String type, boolean requis) {
    }

    public record Document(String face, String libelle, boolean requis) {
    }
}
