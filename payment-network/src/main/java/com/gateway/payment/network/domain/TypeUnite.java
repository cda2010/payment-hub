package com.gateway.payment.network.domain;

/** Les rattachements autorisés entre types sont décrits par {@code RegleRattachement}. */
public enum TypeUnite {
    /** Racine de l'arbre, dans une zone ; code ISO 3166 alpha-2. */
    PAYS("Pays"),
    FSE("FSE"),
    AGENCE("Agence"),
    MASTER("Master"),
    MARCHAND("Marchand"),
    SOUS_DISTRIBUTEUR("Sous-distributeur"),
    /**
     * Se rattache à une unité de n'importe quel niveau ; n'a pas d'enfant mais des membres, pris dans la branche
     * de son parent, qu'il couvre avec toute leur descendance.
     */
    REGROUPEMENT("Regroupement");

    private final String libelle;

    TypeUnite(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    public boolean estRegroupement() {
        return this == REGROUPEMENT;
    }
}
