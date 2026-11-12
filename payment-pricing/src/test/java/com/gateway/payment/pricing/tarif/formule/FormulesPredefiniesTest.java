package com.gateway.payment.pricing.tarif.formule;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.formule.predefinies.AxPlusYFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.ExcessAxPlusYFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.ExcessCappedFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.FixedFormula;
import com.gateway.payment.pricing.tarif.formule.predefinies.PercentTotalFormula;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormulesPredefiniesTest {

    private static final ParametresValides TRANCHE_2 = p(Map.of(
            "seuil", new BigDecimal("8000"), "taux", new BigDecimal("0.015"), "fraisFixes", new BigDecimal("100")));
    private static final ParametresValides TRANCHE_3 = p(Map.of(
            "seuil", new BigDecimal("25000"), "taux", new BigDecimal("0.01"), "fraisFixes", new BigDecimal("200")));
    private static final ParametresValides TRANCHE_4 = p(Map.of(
            "seuil", new BigDecimal("100000"), "taux", new BigDecimal("0.005"),
            "fraisFixes", new BigDecimal("500"), "plafond", new BigDecimal("2000")));

    private final ExcessAxPlusYFormula excess = new ExcessAxPlusYFormula();
    private final ExcessCappedFormula capped = new ExcessCappedFormula();

    @Test
    void ta001_montantEgalAuSeuilDonneZero() {
        ResultatFormule r = excess.calculer(new BigDecimal("8000"), TRANCHE_2);

        assertThat(r.frais()).isEqualByComparingTo("0");
        assertThat(r.baseTaxable()).isEqualByComparingTo("0");
    }

    @Test
    void ta002_unFrancAuDessusDuSeuilDonne100_015AvantArrondi() {
        ResultatFormule r = excess.calculer(new BigDecimal("8001"), TRANCHE_2);

        assertThat(r.frais()).isEqualByComparingTo("100.015");
        assertThat(r.baseTaxable()).isEqualByComparingTo("1");
    }

    @Test
    void ta003_20000DonneFrais280EtDetailLisible() {
        ResultatFormule r = excess.calculer(new BigDecimal("20000"), TRANCHE_2);

        assertThat(r.frais()).isEqualByComparingTo("280");
        assertThat(r.baseTaxable()).isEqualByComparingTo("12000");
        assertThat(r.detail()).isEqualTo("(20000 - 8000) * 0.015 + 100");
    }

    @Test
    void ta004_50000DonneFrais450() {
        assertThat(excess.calculer(new BigDecimal("50000"), TRANCHE_3).frais()).isEqualByComparingTo("450");
    }

    @Test
    void ta005_200000DonneFrais1000SousLePlafond() {
        ResultatFormule r = capped.calculer(new BigDecimal("200000"), TRANCHE_4);

        assertThat(r.frais()).isEqualByComparingTo("1000");
        assertThat(r.detail()).isEqualTo("min((200000 - 100000) * 0.005 + 500, 2000)");
    }

    @Test
    void excessCappedEstPlafonne() {
        assertThat(capped.calculer(new BigDecimal("500000"), TRANCHE_4).frais()).isEqualByComparingTo("2000");
    }

    @Test
    void excessSansFraisFixesUtiliseZero() {
        ResultatFormule r = excess.calculer(new BigDecimal("10000"),
                p(Map.of("seuil", new BigDecimal("8000"), "taux", new BigDecimal("0.01"))));

        assertThat(r.frais()).isEqualByComparingTo("20");
        assertThat(r.detail()).isEqualTo("(10000 - 8000) * 0.01");
    }

    @Test
    void fixedRenvoieLeMontantFixe() {
        ResultatFormule r = new FixedFormula().calculer(new BigDecimal("123456"),
                p(Map.of("montantFixe", new BigDecimal("5000"))));

        assertThat(r.frais()).isEqualByComparingTo("5000");
    }

    @Test
    void percentTotalEtAxPlusY() {
        BigDecimal montant = new BigDecimal("100000");

        assertThat(new PercentTotalFormula().calculer(montant, p(Map.of("taux", new BigDecimal("0.0035")))).frais())
                .isEqualByComparingTo("350");
        assertThat(new AxPlusYFormula().calculer(montant,
                p(Map.of("taux", new BigDecimal("0.005"), "fraisFixes", new BigDecimal("200")))).frais())
                .isEqualByComparingTo("700");
    }

    @Test
    void lesCalculsGardentToutesLesDecimales() {
        ResultatFormule r = new PercentTotalFormula().calculer(new BigDecimal("1"),
                p(Map.of("taux", new BigDecimal("0.000001"))));

        assertThat(r.frais()).isEqualTo(new BigDecimal("0.000001"));
    }

    @Test
    void leRegistreRefuseUneFormuleEnregistreeDeuxFois() {
        assertThatThrownBy(() -> new FormulaRegistry(List.of(new FixedFormula(), new FixedFormula())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FIXED v1");
    }

    @Test
    void leRegistreTrouveParCodeEtVersion() {
        FormulaRegistry registry = JeuDeDonnees.registry();

        assertThat(registry.trouver("EXCESS_CAPPED", 1)).isPresent();
        assertThat(registry.trouver("EXCESS_CAPPED", 2)).isEmpty();
    }

    private static ParametresValides p(Map<String, Object> valeurs) {
        return new ParametresValides(valeurs);
    }
}
