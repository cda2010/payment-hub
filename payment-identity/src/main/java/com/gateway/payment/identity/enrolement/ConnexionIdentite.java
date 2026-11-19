package com.gateway.payment.identity.enrolement;

import java.util.UUID;

/** Ouvre l'accès de connexion : utilisateur canal et compte Keycloak. */
public interface ConnexionIdentite {

    /** Identifiant déjà ouvert pour ce téléphone, ou vide. */
    String identifiant(String tenantId, String telephone);

    void ouvrir(
            String tenantId,
            UUID partyId,
            String telephone,
            String prenom,
            String nom,
            String email,
            String motDePasse
    );
}
