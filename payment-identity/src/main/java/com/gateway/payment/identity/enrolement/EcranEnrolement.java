package com.gateway.payment.identity.enrolement;

import java.util.List;

/** Un écran du workflow ENROLEMENT, tel que le formulaire catalogue le décrit. */
public record EcranEnrolement(
        String etape,
        String formKey,
        String titre,
        List<Champ> champs
) {
    public record Champ(String code, String libelle, String type, boolean requis, List<Option> options) {
    }

    public record Option(String libelle, String valeur) {
    }
}
