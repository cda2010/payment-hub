package com.gateway.payment.pricing.tarif.moteur;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Contexte de calcul transmis par l'application consommatrice (EF-013).
 *
 * @param canalCode         vide = seules les grilles « tous canaux » sont candidates
 * @param dateCalcul        vide = date du jour
 * @param source            où l'opération commence ; vide = seules les grilles « toutes sources » sont candidates
 * @param destination       où elle aboutit ; vide = seules les grilles « toutes destinations » sont candidates
 * @param montant           principal de l'opération (toujours requis)
 * @param convertedAmount   montant FX (pour {@code CONVERTED_AMOUNT})
 * @param feeBaseOverride   override explicite / {@code CUSTOM}
 * @param aggregates        cumuls ({@code cumulAmount}, {@code cumulAmount.MONTH.AGENT}, …)
 */
public record DemandeFrais(
        String produitCode,
        String canalCode,
        String devise,
        BigDecimal montant,
        LocalDate dateCalcul,
        Localisation source,
        Localisation destination,
        BigDecimal convertedAmount,
        BigDecimal feeBaseOverride,
        Map<String, BigDecimal> aggregates
) {

    public DemandeFrais {
        source = source == null ? Localisation.AUCUNE : source;
        destination = destination == null ? Localisation.AUCUNE : destination;
        aggregates = aggregates == null ? Map.of() : aggregates;
    }

    public DemandeFrais(String produitCode, String canalCode, String devise, BigDecimal montant, LocalDate dateCalcul) {
        this(produitCode, canalCode, devise, montant, dateCalcul, null, null, null, null, Map.of());
    }

    public DemandeFrais(String produitCode, String canalCode, String devise, BigDecimal montant, LocalDate dateCalcul,
                        Localisation source, Localisation destination) {
        this(produitCode, canalCode, devise, montant, dateCalcul, source, destination, null, null, Map.of());
    }
}
