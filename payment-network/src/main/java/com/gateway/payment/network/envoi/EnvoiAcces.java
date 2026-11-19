package com.gateway.payment.network.envoi;

/** Transmet le mot de passe temporaire. Le réseau ne le conserve pas. */
public interface EnvoiAcces {

    void envoyer(String canal, String destinataire, String login, String motDePasse);
}
