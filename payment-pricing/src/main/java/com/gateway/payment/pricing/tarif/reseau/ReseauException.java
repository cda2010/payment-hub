package com.gateway.payment.pricing.tarif.reseau;

import lombok.Getter;

/** Échec d'une lecture du réseau : élément introuvable (à traduire selon le contexte) ou service indisponible. */
@Getter
public class ReseauException extends RuntimeException {

    private final boolean introuvable;

    public ReseauException(boolean introuvable, String message) {
        super(message);
        this.introuvable = introuvable;
    }
}
