package com.gateway.payment.pricing.tarif;

import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Erreur fonctionnelle du moteur ou de l'administration des grilles, avec un code stable pour les clients. */
@Getter
public class TarifException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<ValidationIssue> details;

    public TarifException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public TarifException(HttpStatus status, String code, String message, List<ValidationIssue> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = List.copyOf(details);
    }

    public static TarifException introuvable(String code, String message) {
        return new TarifException(HttpStatus.NOT_FOUND, code, message);
    }

    public static TarifException conflit(String code, String message) {
        return new TarifException(HttpStatus.CONFLICT, code, message);
    }

    public static TarifException configurationInvalide(String message, List<ValidationIssue> details) {
        return new TarifException(HttpStatus.BAD_REQUEST, "CONFIGURATION_INVALIDE", message, details);
    }

    /** Le calcul ne peut pas aboutir avec la configuration publiée (aucune grille, aucune tranche…). */
    public static TarifException calcul(String code, String message) {
        return new TarifException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }

    public static TarifException calcul(String code, String message, List<ValidationIssue> details) {
        return new TarifException(HttpStatus.UNPROCESSABLE_ENTITY, code, message, details);
    }
}
