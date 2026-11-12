package com.gateway.payment.divers.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CoffreGoal {
    VOYAGE("voyage"),
    PROJET("projet"),
    URGENCE("urgence"),
    AUTRE("autre");

    private final String code;

    CoffreGoal(String code) {
        this.code = code;
    }

    @JsonValue
    public String code() {
        return code;
    }

    @JsonCreator
    public static CoffreGoal from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Objectif du coffre requis");
        }
        String key = raw.trim().toLowerCase();
        for (CoffreGoal goal : values()) {
            if (goal.code.equals(key) || goal.name().equalsIgnoreCase(key)) {
                return goal;
            }
        }
        throw new IllegalArgumentException("Objectif de coffre inconnu : " + raw);
    }
}
