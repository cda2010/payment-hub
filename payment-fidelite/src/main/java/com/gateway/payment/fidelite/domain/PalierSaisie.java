package com.gateway.payment.fidelite.domain;

import java.math.BigDecimal;
import java.util.List;

/** Palier demandé à la création d'un programme. */
public record PalierSaisie(
        String code,
        String label,
        int seuilPoints,
        BigDecimal pointsParMille,
        List<String> avantages
) {
}
