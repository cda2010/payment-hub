package com.gateway.payment.billing.connector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Facture normalisée connecteur bill-payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Bill(
        String reference,
        String montant,
        String amountDue,
        String libelle,
        String currency,
        String dueDate,
        String status,
        List<MParam> extendedData
) {
    public Bill {
        // amountDue et montant restent synchrones si un seul est fourni
        if ((amountDue == null || amountDue.isBlank()) && montant != null && !montant.isBlank()) {
            amountDue = montant;
        }
        if ((montant == null || montant.isBlank()) && amountDue != null && !amountDue.isBlank()) {
            montant = amountDue;
        }
        extendedData = extendedData != null ? List.copyOf(extendedData) : List.of();
    }

    public BigDecimal montantAsDecimal() {
        String raw = amountDue != null && !amountDue.isBlank() ? amountDue : montant;
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String param(String code) {
        if (code == null || extendedData == null) {
            return null;
        }
        for (MParam p : extendedData) {
            if (p != null && code.equalsIgnoreCase(p.code())) {
                return p.valeur();
            }
        }
        return null;
    }
}
