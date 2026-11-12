package com.gateway.payment.network;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Erreur fonctionnelle du référentiel réseau, avec un code stable pour les clients. */
@Getter
public class NetworkException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public NetworkException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static NetworkException introuvable(String code, String message) {
        return new NetworkException(HttpStatus.NOT_FOUND, code, message);
    }

    public static NetworkException conflit(String code, String message) {
        return new NetworkException(HttpStatus.CONFLICT, code, message);
    }

    /** Règle de gestion du réseau non respectée (rattachement, zone, regroupement…). */
    public static NetworkException regle(String code, String message) {
        return new NetworkException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
