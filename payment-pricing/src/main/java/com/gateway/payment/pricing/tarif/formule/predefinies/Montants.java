package com.gateway.payment.pricing.tarif.formule.predefinies;

import java.math.BigDecimal;

final class Montants {

    private Montants() {
    }

    /** Écriture courte d'un nombre pour le détail du calcul : 20000.00 → 20000, 0.0150 → 0.015. */
    static String texte(BigDecimal value) {
        return value.signum() == 0 ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
