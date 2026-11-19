package com.gateway.payment.network.connexion;

import java.util.UUID;

/** Ouvre le compte de connexion d'une personne de l'unité. Le mot de passe reste chez Keycloak. */
public interface ConnexionGuichet {

    void ouvrir(
            UUID utilisateurId,
            UUID uniteId,
            String identifiant,
            String nom,
            String roleMetier,
            String audience,
            String motDePasse,
            String email
    );

    /** Active ou désactive le compte de connexion. Un compte désactivé ne peut plus entrer. */
    void definirAcces(String identifiant, boolean actif);

    /**
     * Ajoute le rôle d'un rattachement sur le compte déjà ouvert.
     * Le mot de passe n'est pas changé.
     */
    void rattacher(String identifiant, String roleMetier);

    void reinitialiserMotDePasse(String identifiant, String motDePasse);
}
