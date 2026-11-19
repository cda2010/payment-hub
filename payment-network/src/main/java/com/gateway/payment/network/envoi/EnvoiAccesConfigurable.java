package com.gateway.payment.network.envoi;

import com.gateway.payment.network.NetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Envoie le mot de passe par la passerelle configurée.
 * Sans passerelle, le message est consigné : c'est le relais local, le temps de brancher le SMS ou le courrier.
 */
@Component
public class EnvoiAccesConfigurable implements EnvoiAcces {

    private static final Logger JOURNAL = LoggerFactory.getLogger(EnvoiAccesConfigurable.class);

    private final String smsUrl;
    private final String mailUrl;
    private final RestClient http;

    public EnvoiAccesConfigurable(
            @Value("${network.envoi.sms-url:}") String smsUrl,
            @Value("${network.envoi.mail-url:}") String mailUrl
    ) {
        this.smsUrl = smsUrl == null ? "" : smsUrl.trim();
        this.mailUrl = mailUrl == null ? "" : mailUrl.trim();
        this.http = RestClient.create();
    }

    @Override
    public void envoyer(String canal, String destinataire, String login, String motDePasse) {
        String message = "Coris : votre login est " + login
                + ". Mot de passe temporaire : " + motDePasse
                + ". Changez-le à la première connexion.";
        String url = "SMS".equals(canal) ? smsUrl : mailUrl;
        if (url.isBlank()) {
            JOURNAL.info("Accès temporaire {} vers {} pour {} : {}", canal, destinataire, login, motDePasse);
            return;
        }
        try {
            http.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "canal", canal,
                            "destinataire", destinataire,
                            "login", login,
                            "message", message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            throw new NetworkException(HttpStatus.BAD_GATEWAY, "ENVOI_INDISPONIBLE",
                    "Le mot de passe n'a pas pu être envoyé par " + ("SMS".equals(canal) ? "SMS" : "e-mail") + ".");
        }
    }
}
