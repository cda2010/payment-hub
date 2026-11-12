package com.gateway.payment.billing.connector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Paramètre étendu d'une facture connecteur ({@code code}/{@code valeur}/{@code affiche}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MParam(
        String code,
        String label,
        String valeur,
        Boolean affiche
) {
    public boolean isAffiche() {
        return affiche == null || affiche;
    }
}
