package com.gateway.payment.pricing.tarif.formule;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Valeurs typées après validation (BigDecimal, Long, Boolean, String), valeurs par défaut comprises.
 * Un paramètre facultatif sans valeur ni défaut est absent.
 */
public record ParametresValides(Map<String, Object> valeurs) {

    public ParametresValides {
        valeurs = Collections.unmodifiableMap(valeurs);
    }

    public BigDecimal decimal(String code) {
        return (BigDecimal) valeurs.get(code);
    }

    /** Valeur DECIMAL, ou zéro si le paramètre facultatif est absent. */
    public BigDecimal decimalOuZero(String code) {
        BigDecimal value = decimal(code);
        return value != null ? value : BigDecimal.ZERO;
    }
}
