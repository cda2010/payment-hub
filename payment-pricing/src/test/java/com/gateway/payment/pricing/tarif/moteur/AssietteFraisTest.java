package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.domain.BaseCalculFrais;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.PeriodeCumul;
import com.gateway.payment.pricing.tarif.domain.ScopeCumul;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssietteFraisTest {

    @Test
    void transactionAmountParDefaut() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        DemandeFrais d = new DemandeFrais("P", null, "XOF", new BigDecimal("1000"), null);
        AssietteFrais.Resolution r = AssietteFrais.resoudre(g, d);
        assertThat(r.montant()).isEqualByComparingTo("1000.00");
        assertThat(r.baseCalcul()).isEqualTo(BaseCalculFrais.TRANSACTION_AMOUNT);
    }

    @Test
    void convertedAmount() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.setBaseCalcul(BaseCalculFrais.CONVERTED_AMOUNT);
        DemandeFrais d = new DemandeFrais("P", null, "XOF", new BigDecimal("1000"), null,
                null, null, new BigDecimal("1500"), null, Map.of());
        assertThat(AssietteFrais.resoudre(g, d).montant()).isEqualByComparingTo("1500.00");
    }

    @Test
    void cumulAmountAvecPeriodeEtScope() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.setBaseCalcul(BaseCalculFrais.CUMUL_AMOUNT);
        g.setCumulPeriod(PeriodeCumul.MONTH);
        g.setCumulScope(ScopeCumul.AGENT);
        DemandeFrais d = new DemandeFrais("P", null, "XOF", new BigDecimal("1000"), null,
                null, null, null, null, Map.of("cumulAmount.MONTH.AGENT", new BigDecimal("50000")));
        AssietteFrais.Resolution r = AssietteFrais.resoudre(g, d);
        assertThat(r.montant()).isEqualByComparingTo("50000.00");
        assertThat(r.period()).isEqualTo(PeriodeCumul.MONTH);
        assertThat(r.scope()).isEqualTo(ScopeCumul.AGENT);
    }

    @Test
    void cumulManquantRefuse() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        g.setBaseCalcul(BaseCalculFrais.CUMUL_AMOUNT);
        g.setCumulPeriod(PeriodeCumul.MONTH);
        g.setCumulScope(ScopeCumul.AGENT);
        DemandeFrais d = new DemandeFrais("P", null, "XOF", new BigDecimal("1000"), null);
        assertThatThrownBy(() -> AssietteFrais.resoudre(g, d))
                .isInstanceOf(TarifException.class)
                .extracting(e -> ((TarifException) e).getCode())
                .isEqualTo("ASSIETTE_CUMUL_MANQUANTE");
    }

    @Test
    void overrideExplicite() {
        GrilleTarifaire g = JeuDeDonnees.grille("G", "P", null, "XOF");
        DemandeFrais d = new DemandeFrais("P", null, "XOF", new BigDecimal("1000"), null,
                null, null, null, new BigDecimal("777"), Map.of());
        assertThat(AssietteFrais.resoudre(g, d).montant()).isEqualByComparingTo("777.00");
    }
}
