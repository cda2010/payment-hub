package com.gateway.payment.pricing.tarif.formule;

import com.gateway.payment.pricing.tarif.domain.TypeDonnee;

public record ParametreAttendu(String code, TypeDonnee type, boolean obligatoire) {

    public static ParametreAttendu decimalObligatoire(String code) {
        return new ParametreAttendu(code, TypeDonnee.DECIMAL, true);
    }

    public static ParametreAttendu decimalFacultatif(String code) {
        return new ParametreAttendu(code, TypeDonnee.DECIMAL, false);
    }
}
