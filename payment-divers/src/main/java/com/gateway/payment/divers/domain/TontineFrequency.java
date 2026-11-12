package com.gateway.payment.divers.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TontineFrequency {
    HEBDOMADAIRE("Hebdomadaire"),
    MENSUELLE("Mensuelle");

    private final String label;

    TontineFrequency(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static TontineFrequency from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Fréquence requise");
        }
        String key = raw.trim();
        for (TontineFrequency frequency : values()) {
            if (frequency.label.equalsIgnoreCase(key) || frequency.name().equalsIgnoreCase(key)) {
                return frequency;
            }
        }
        throw new IllegalArgumentException("Fréquence inconnue : " + raw);
    }
}
