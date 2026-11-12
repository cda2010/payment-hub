package com.gateway.payment.fidelite.domain;

/** Récompense du catalogue posé sur un programme. */
public record RecompenseSaisie(
        String code,
        String titre,
        String description,
        int cout,
        String icone,
        String categorie
) {
}
