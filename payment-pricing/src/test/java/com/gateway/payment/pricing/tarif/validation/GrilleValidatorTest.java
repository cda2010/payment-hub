package com.gateway.payment.pricing.tarif.validation;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GrilleValidatorTest {

    private final GrilleValidator validator = new GrilleValidator(new ParametreValidator());

    @Test
    void grilleDeDemonstrationValide() {
        assertThat(validator.valider(JeuDeDonnees.trfStd())).isEmpty();
    }

    @Test
    void ta008_chevauchementDetecte() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.getTranches().get(1).setMontantMin(new BigDecimal("7000"));

        assertThat(validator.valider(g)).singleElement().satisfies(e -> {
            assertThat(e.code()).isEqualTo("TRANCHES_CHEVAUCHEMENT");
            assertThat(e.tranche()).isEqualTo(2);
            assertThat(e.message()).contains("7000", "8000");
        });
    }

    @Test
    void bornesIdentiquesSeChevauchent() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.getTranches().get(1).setMontantMin(new BigDecimal("8000"));

        assertThat(validator.valider(g)).extracting(ValidationIssue::code).containsExactly("TRANCHES_CHEVAUCHEMENT");
    }

    @Test
    void ta009_trouDetecteSiContinuiteRequise() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.getTranches().get(2).setMontantMin(new BigDecimal("26000"));

        assertThat(validator.valider(g)).singleElement().satisfies(e -> {
            assertThat(e.code()).isEqualTo("TRANCHES_DISCONTINUES");
            assertThat(e.tranche()).isEqualTo(3);
        });

        g.setContinuiteRequise(false);
        assertThat(validator.valider(g)).isEmpty();
    }

    @Test
    void uniteDeContinuiteSuitLesDecimalesDesBornes() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.ajouterTranche(JeuDeDonnees.tranche(1, "0", "50000", JeuDeDonnees.fixed(), Map.of("montantFixe", 100)));
        g.ajouterTranche(JeuDeDonnees.tranche(2, "50000.01", null, JeuDeDonnees.fixed(), Map.of("montantFixe", 200)));

        assertThat(validator.valider(g)).isEmpty();
    }

    @Test
    void grilleSansTrancheEtDatesInvalides() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.setDateFin(LocalDate.of(2025, 12, 31));

        assertThat(validator.valider(g)).extracting(ValidationIssue::code)
                .containsExactly("GRILLE_DATES_INVALIDES", "GRILLE_SANS_TRANCHE");
    }

    @Test
    void trancheOuverteAilleursQuEnDernier() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.ajouterTranche(JeuDeDonnees.tranche(1, "0", null, JeuDeDonnees.fixed(), Map.of("montantFixe", 100)));
        g.ajouterTranche(JeuDeDonnees.tranche(2, "1000", "2000", JeuDeDonnees.fixed(), Map.of("montantFixe", 200)));

        assertThat(validator.valider(g)).extracting(ValidationIssue::code).containsExactly("TRANCHE_OUVERTE_NON_DERNIERE");
    }

    @Test
    void bornesEtFraisInvalides() {
        TrancheTarifaire t = JeuDeDonnees.tranche(1, "1000", "500", JeuDeDonnees.fixed(), Map.of("montantFixe", 100));
        t.setFraisMin(new BigDecimal("300"));
        t.setFraisMax(new BigDecimal("200"));

        assertThat(validator.validerTranche(t)).extracting(ValidationIssue::code, ValidationIssue::champ).containsExactly(
                org.assertj.core.groups.Tuple.tuple("TRANCHE_BORNES_INVALIDES", "montantMax"),
                org.assertj.core.groups.Tuple.tuple("TRANCHE_FRAIS_INVALIDES", "fraisMax"));
    }

    @Test
    void bornesAvecTropDeDecimalesRefusees() {
        TrancheTarifaire t = JeuDeDonnees.tranche(1, "0.001", "500", JeuDeDonnees.fixed(), Map.of("montantFixe", 100));

        assertThat(validator.validerTranche(t)).extracting(ValidationIssue::code).containsExactly("TRANCHE_BORNES_INVALIDES");
    }

    @Test
    void erreursDeParametresRemonteesAvecLaTranche() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.getTranches().get(3).getParametres().remove("plafond");

        assertThat(validator.valider(g)).singleElement().satisfies(e -> {
            assertThat(e.code()).isEqualTo("PARAM_OBLIGATOIRE_MANQUANT");
            assertThat(e.tranche()).isEqualTo(4);
            assertThat(e.champ()).isEqualTo("parametres.plafond");
        });
    }
}
