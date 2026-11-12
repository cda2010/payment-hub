package com.gateway.payment.billing.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Rôle métier d'un champ UI produit.
 * Accepte aussi les alias Omnicanal ({@code REFERENCE}, {@code MONTANT}, …).
 */
public enum ProductFieldRole {
    SUBSCRIBER,
    BILL_REF,
    AMOUNT,
    EXTRA;

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public static ProductFieldRole fromJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return EXTRA;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return switch (n) {
            case "SUBSCRIBER", "ABONNE", "SUBSCRIBER_REF", "SUBSCRIBERREFERENCE" -> SUBSCRIBER;
            case "BILL_REF", "BILLREF", "BILL_REFERENCE", "BILLREFERENCE",
                 "REFERENCE", "REF", "METER", "METERNUM", "METER_NUMBER", "METERNUMBER" -> BILL_REF;
            case "AMOUNT", "MONTANT", "AMOUNT_DUE", "AMOUNTDUE" -> AMOUNT;
            case "EXTRA", "OTHER", "AUTRE" -> EXTRA;
            default -> {
                try {
                    yield ProductFieldRole.valueOf(n);
                } catch (IllegalArgumentException ex) {
                    yield EXTRA;
                }
            }
        };
    }
}
