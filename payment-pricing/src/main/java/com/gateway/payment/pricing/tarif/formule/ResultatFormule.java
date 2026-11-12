package com.gateway.payment.pricing.tarif.formule;

import java.math.BigDecimal;

/**
 * @param baseTaxable part du montant à laquelle la formule s'applique
 * @param frais       frais bruts, avant plancher/plafond de la tranche et arrondi
 * @param detail      calcul lisible, ex. {@code (20000 - 8000) * 0.015 + 100}
 */
public record ResultatFormule(BigDecimal baseTaxable, BigDecimal frais, String detail) {
}
