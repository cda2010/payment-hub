package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.ReseauDeTest;
import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import com.gateway.payment.pricing.tarif.validation.ParametreValidator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MoteurFraisTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 11);

    private final GrilleTarifaireRepository grilles = mock(GrilleTarifaireRepository.class);
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final MoteurFrais moteur = new MoteurFrais(grilles, new ParametreValidator(), JeuDeDonnees.registry(),
            new ReseauDeTest(), Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneOffset.UTC), meters);

    @Test
    void ta003_calculCompletAvecDetailEtVersions() {
        GrilleTarifaire trf = JeuDeDonnees.trfStd();
        when(grilles.candidates(eq("TRANSFERT"), eq("MOBILE_MONEY"), eq("XOF"), eq(AUJOURDHUI), any(), any())).thenReturn(List.of(trf));

        ResultatFrais r = moteur.calculer(new DemandeFrais("transfert", "mobile_money", "xof", new BigDecimal("20000"), null));

        assertThat(r.frais()).isEqualByComparingTo("280");
        assertThat(r.grilleCode()).isEqualTo("TRF_STD");
        assertThat(r.versionGrille()).isEqualTo(1);
        assertThat(r.tranche().ordre()).isEqualTo(2);
        assertThat(r.tranche().montantMin()).isEqualByComparingTo("8001");
        assertThat(r.formuleCode()).isEqualTo("EXCESS_AX_PLUS_Y");
        assertThat(r.versionFormule()).isEqualTo(1);
        assertThat(r.baseTaxable()).isEqualByComparingTo("12000");
        assertThat(r.details()).isEqualTo("(20000 - 8000) * 0.015 + 100");
        assertThat(r.parametres()).containsKeys("seuil", "taux", "fraisFixes");
        assertThat(r.dateCalcul()).isEqualTo(AUJOURDHUI);
        assertThat(meters.get("pricing.frais.calcul").tag("issue", "OK").timer().count()).isEqualTo(1);
    }

    @Test
    void ta002_arrondiALUniteHalfUp() {
        ResultatFrais r = moteur.appliquer(JeuDeDonnees.trfStd(), new BigDecimal("8001"), AUJOURDHUI);

        assertThat(r.fraisAvantArrondi()).isEqualByComparingTo("100.015");
        assertThat(r.frais()).isEqualTo(new BigDecimal("100"));
        assertThat(r.arrondi()).isEqualTo(new ResultatFrais.Arrondi(0, RoundingMode.HALF_UP));
    }

    @Test
    void arrondiConfigurableParGrille() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.setArrondiEchelle(2);
        g.setArrondiMode(RoundingMode.CEILING);

        assertThat(moteur.appliquer(g, new BigDecimal("8001"), AUJOURDHUI).frais()).isEqualTo(new BigDecimal("100.02"));
    }

    @Test
    void bornesIncluses() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();

        assertThat(moteur.appliquer(g, new BigDecimal("8000"), AUJOURDHUI).tranche().ordre()).isEqualTo(1);
        assertThat(moteur.appliquer(g, new BigDecimal("8000"), AUJOURDHUI).frais()).isEqualByComparingTo("0");
        assertThat(moteur.appliquer(g, new BigDecimal("25000"), AUJOURDHUI).tranche().ordre()).isEqualTo(2);
        assertThat(moteur.appliquer(g, new BigDecimal("25001"), AUJOURDHUI).tranche().ordre()).isEqualTo(3);
        assertThat(moteur.appliquer(g, new BigDecimal("50000"), AUJOURDHUI).frais()).isEqualByComparingTo("450");
        assertThat(moteur.appliquer(g, new BigDecimal("200000"), AUJOURDHUI).frais()).isEqualByComparingTo("1000");
    }

    @Test
    void ta010_montantHorsTrancheErreurExplicite() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();

        assertThatThrownBy(() -> moteur.appliquer(g, new BigDecimal("600000"), AUJOURDHUI))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("AUCUNE_TRANCHE");
                    assertThat(e.getStatus().value()).isEqualTo(422);
                    assertThat(e.getMessage()).doesNotContain("600000");
                });
        assertThatThrownBy(() -> moteur.appliquer(g, new BigDecimal("8000.5"), AUJOURDHUI))
                .as("un montant entre deux bornes n'est jamais rattaché arbitrairement")
                .hasFieldOrPropertyWithValue("code", "AUCUNE_TRANCHE");
    }

    @Test
    void plancherEtPlafondDeLaTranche() {
        GrilleTarifaire g = JeuDeDonnees.grille("FEE-DOMESTIC", "ACCOUNT_TRANSFER", "WEB", "XOF");
        TrancheTarifaire t = JeuDeDonnees.tranche(1, "0", null, JeuDeDonnees.axPlusY(),
                Map.of("taux", new BigDecimal("0.005"), "fraisFixes", 200));
        t.setFraisMin(new BigDecimal("300"));
        t.setFraisMax(new BigDecimal("2500"));
        g.ajouterTranche(t);

        ResultatFrais bas = moteur.appliquer(g, new BigDecimal("10000"), AUJOURDHUI);
        assertThat(bas.fraisFormule()).isEqualByComparingTo("250");
        assertThat(bas.ajustement()).isEqualTo(MoteurFrais.PLANCHER);
        assertThat(bas.frais()).isEqualByComparingTo("300");

        ResultatFrais haut = moteur.appliquer(g, new BigDecimal("1000000"), AUJOURDHUI);
        assertThat(haut.ajustement()).isEqualTo(MoteurFrais.PLAFOND);
        assertThat(haut.frais()).isEqualByComparingTo("2500");

        assertThat(moteur.appliquer(g, new BigDecimal("100000"), AUJOURDHUI).ajustement()).isNull();
    }

    @Test
    void grilleDuCanalPrecisPrioritaireSurTousCanaux() {
        GrilleTarifaire tousCanaux = JeuDeDonnees.grille("BILL-ALL", "BILL_PAYMENT", null, "XOF");
        tousCanaux.ajouterTranche(JeuDeDonnees.tranche(1, "0", null, JeuDeDonnees.fixed(), Map.of("montantFixe", 50)));
        GrilleTarifaire web = JeuDeDonnees.grille("BILL-WEB", "BILL_PAYMENT", "WEB", "XOF");
        web.ajouterTranche(JeuDeDonnees.tranche(1, "0", null, JeuDeDonnees.fixed(), Map.of("montantFixe", 25)));
        when(grilles.candidates(eq("BILL_PAYMENT"), eq("WEB"), eq("XOF"), any(), any(), any())).thenReturn(List.of(tousCanaux, web));

        ResultatFrais r = moteur.calculer(new DemandeFrais("BILL_PAYMENT", "WEB", "XOF", new BigDecimal("1000"), null));

        assertThat(r.grilleCode()).isEqualTo("BILL-WEB");
        assertThat(r.frais()).isEqualByComparingTo("25");
    }

    @Test
    void deuxGrillesAuMemeNiveauSontAmbigues() {
        GrilleTarifaire a = JeuDeDonnees.grille("A", "P", "WEB", "XOF");
        GrilleTarifaire b = JeuDeDonnees.grille("B", "P", "WEB", "XOF");
        when(grilles.candidates(any(), any(), any(), any(), any(), any())).thenReturn(List.of(a, b));

        assertThatThrownBy(() -> moteur.calculer(new DemandeFrais("P", "WEB", "XOF", BigDecimal.TEN, null)))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("GRILLE_AMBIGUE");
                    assertThat(e.getMessage()).contains("A v1", "B v1");
                });
        assertThat(meters.get("pricing.frais.calcul").tag("issue", "GRILLE_AMBIGUE").timer().count()).isEqualTo(1);
    }

    @Test
    void aucuneGrilleApplicable() {
        when(grilles.candidates(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> moteur.calculer(new DemandeFrais("P", null, "XOF", BigDecimal.TEN,
                LocalDate.of(2025, 6, 1))))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("AUCUNE_GRILLE_APPLICABLE");
                    assertThat(e.getMessage()).contains("tous canaux", "2025-06-01");
                });
    }

    @Test
    void montantNegatifRefuse() {
        assertThatThrownBy(() -> moteur.calculer(new DemandeFrais("P", null, "XOF", new BigDecimal("-1"), null)))
                .hasFieldOrPropertyWithValue("code", "MONTANT_INVALIDE");
    }

    @Test
    void parametresInvalidesEnBaseBloquentLeCalcul() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.getTranches().get(1).getParametres().put("taux", new BigDecimal("2"));

        assertThatThrownBy(() -> moteur.appliquer(g, new BigDecimal("20000"), AUJOURDHUI))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("CONFIGURATION_INVALIDE");
                    assertThat(e.getDetails()).singleElement()
                            .satisfies(i -> assertThat(i.champ()).isEqualTo("parametres.taux"));
                });
    }

    @Test
    void essaiDeFormuleAvecPlancherSansArrondi() {
        EssaiFormule essai = moteur.essayer(JeuDeDonnees.excess(),
                Map.of("seuil", 8000, "taux", new BigDecimal("0.015"), "fraisFixes", 100),
                new BigDecimal("8001"), new BigDecimal("150"), null);

        assertThat(essai.valide()).isTrue();
        assertThat(essai.fraisFormule()).isEqualByComparingTo("100.015");
        assertThat(essai.ajustement()).isEqualTo(MoteurFrais.PLANCHER);
        assertThat(essai.frais()).isEqualByComparingTo("150");
        assertThat(essai.details()).isEqualTo("(8001 - 8000) * 0.015 + 100");
    }

    @Test
    void essaiDeFormuleAvecParametresInvalidesRenvoieLesErreurs() {
        EssaiFormule essai = moteur.essayer(JeuDeDonnees.excess(), Map.of("seuil", 8000),
                new BigDecimal("20000"), null, null);

        assertThat(essai.valide()).isFalse();
        assertThat(essai.erreurs()).extracting(e -> e.champ()).containsExactly("parametres.taux");
        assertThat(essai.frais()).isNull();
    }

    @Test
    void ta015_simulationDetailTrancheEtVersions() {
        ResultatFrais r = moteur.appliquer(JeuDeDonnees.trfStd(), new BigDecimal("200000"), AUJOURDHUI);

        assertThat(r.tranche()).isEqualTo(new ResultatFrais.TrancheRetenue(4, new BigDecimal("100001"),
                new BigDecimal("500000"), null, null));
        assertThat(r.formuleCode()).isEqualTo("EXCESS_CAPPED");
        assertThat(r.versionGrille()).isEqualTo(1);
        assertThat(r.versionFormule()).isEqualTo(1);
        assertThat(r.baseTaxable()).isEqualByComparingTo("100000");
        assertThat(r.details()).isEqualTo("min((200000 - 100000) * 0.005 + 500, 2000)");
        assertThat(r.frais()).isEqualByComparingTo("1000");
    }
}
