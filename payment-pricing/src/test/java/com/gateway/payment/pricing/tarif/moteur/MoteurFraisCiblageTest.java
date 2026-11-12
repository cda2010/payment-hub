package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.ReseauDeTest;
import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais.Correspondance;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import com.gateway.payment.pricing.tarif.validation.ParametreValidator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Sélection de la grille selon la source et la destination de l'opération. */
class MoteurFraisCiblageTest {

    private final GrilleTarifaireRepository grilles = mock(GrilleTarifaireRepository.class);
    private final ReseauDeTest reseau = new ReseauDeTest();
    private final MoteurFrais moteur = new MoteurFrais(grilles, new ParametreValidator(), JeuDeDonnees.registry(),
            reseau, Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneOffset.UTC), new SimpleMeterRegistry());

    @Test
    void lUniteLaPlusProcheDeLaSourceLEmporte() {
        publiees(grille("TOUTES", null, null, 10), grille("BF", source("BF"), null, 20),
                grille("FSE", source("FSE-UEMOA"), null, 30), grille("OHG", source("AG-OUAHIGOUYA"), null, 40));

        ResultatFrais r = calculer(unite("SD-SAHEL-01"), null);

        assertThat(r.grilleCode()).isEqualTo("OHG");
        assertThat(r.frais()).isEqualByComparingTo("40");
        assertThat(r.source()).isEqualTo(new Correspondance(CibleReseau.Niveau.UNITE, "AG-OUAHIGOUYA", null));
        assertThat(r.destination()).isEqualTo(Correspondance.TOUTES);
        assertThat(calculer(unite("AG-KAYA"), null).grilleCode()).isEqualTo("FSE");
        assertThat(calculer(new Localisation(null, "ML", null), null).grilleCode()).isEqualTo("TOUTES");
    }

    @Test
    void unRegroupementPasseApresSonMembreEtAvantLeParentDeCeMembre() {
        publiees(grille("FSE", source("FSE-UEMOA"), null, 30), grille("NORD", source("RG-AGENCES-NORD"), null, 35));

        ResultatFrais r = calculer(unite("SD-SAHEL-01"), null);
        assertThat(r.grilleCode()).isEqualTo("NORD");
        assertThat(r.source()).isEqualTo(new Correspondance(CibleReseau.Niveau.UNITE, "RG-AGENCES-NORD", "AG-OUAHIGOUYA"));

        publiees(grille("NORD", source("RG-AGENCES-NORD"), null, 35), grille("OHG", source("AG-OUAHIGOUYA"), null, 40));
        assertThat(calculer(unite("SD-SAHEL-01"), null).grilleCode()).isEqualTo("OHG");

        publiees(grille("OHG", source("AG-OUAHIGOUYA"), null, 40), grille("EQUIPE", source("RG-EQUIPE-SAHEL"), null, 45));
        assertThat(calculer(unite("SD-SAHEL-01"), null).grilleCode())
                .as("membre direct : le regroupement passe juste après l'unité elle-même")
                .isEqualTo("EQUIPE");
    }

    @Test
    void laZoneEstMoinsPreciseQueLePays() {
        publiees(grille("UEMOA", CibleReseau.zone("UEMOA"), null, 10), grille("BF", source("BF"), null, 20));

        assertThat(calculer(new Localisation(null, "BF", null), null).grilleCode()).isEqualTo("BF");
        ResultatFrais mali = calculer(new Localisation(null, "ML", null), null);
        assertThat(mali.grilleCode()).isEqualTo("UEMOA");
        assertThat(mali.source()).isEqualTo(new Correspondance(CibleReseau.Niveau.ZONE, "UEMOA", null));
    }

    @Test
    void laSourcePasseAvantLaDestinationPuisLeCanal() {
        GrilleTarifaire parSource = grille("KAYA", source("AG-KAYA"), null, 10);
        GrilleTarifaire parDestination = grille("VERS-ML", null, source("ML"), 20);
        GrilleTarifaire parCanal = grille("WEB", null, null, 30);
        parCanal.setCanal("WEB");
        publiees(parSource, parDestination, parCanal);

        assertThat(calculer(unite("AG-KAYA"), new Localisation(null, "ML", null)).grilleCode()).isEqualTo("KAYA");
        ResultatFrais r = calculer(unite("SD-SAHEL-01"), new Localisation(null, "ML", null));
        assertThat(r.grilleCode()).isEqualTo("VERS-ML");
        assertThat(r.destination()).isEqualTo(new Correspondance(CibleReseau.Niveau.UNITE, "ML", null));
        assertThat(calculer(unite("SD-SAHEL-01"), null).grilleCode()).isEqualTo("WEB");
    }

    @Test
    void uneGrilleQuiViseUneDestinationExigeQueLaDemandeEnDonneUne() {
        publiees(grille("VERS-CEMAC", CibleReseau.zone("UEMOA"), CibleReseau.zone("CEMAC"), 500));

        ResultatFrais r = calculer(unite("AG-KAYA"), new Localisation(null, null, "CEMAC"));
        assertThat(r.grilleCode()).isEqualTo("VERS-CEMAC");
        assertThat(r.destination()).isEqualTo(new Correspondance(CibleReseau.Niveau.ZONE, "CEMAC", null));

        assertThatThrownBy(() -> calculer(unite("AG-KAYA"), null))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("AUCUNE_GRILLE_APPLICABLE");
                    assertThat(e.getMessage()).contains("destination (non renseignée)");
                });
    }

    @Test
    void leReseauNEstPasInterrogeSiAucuneGrilleNeCibleCeCote() {
        publiees(grille("TOUTES", null, null, 10));

        assertThat(calculer(unite("SD-SAHEL-01"), new Localisation(null, "ML", null)).grilleCode()).isEqualTo("TOUTES");
        assertThat(reseau.appels).hasValue(0);
    }

    @Test
    void contexteInvalideOuReseauIndisponible() {
        publiees(grille("BF", source("BF"), null, 20));

        assertThatThrownBy(() -> calculer(unite("RG-AGENCES-NORD"), null))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("CONTEXTE_INVALIDE");
                    assertThat(e.getMessage()).contains("regroupement");
                });
        assertThatThrownBy(() -> calculer(new Localisation(UUID.randomUUID(), null, null), null))
                .hasFieldOrPropertyWithValue("code", "CONTEXTE_INVALIDE");

        reseau.indisponible = true;
        assertThatThrownBy(() -> calculer(new Localisation(null, "BF", null), null))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("RESEAU_INDISPONIBLE");
                    assertThat(e.getStatus().value()).isEqualTo(503);
                });
    }

    @Test
    void deuxGrillesDeMemePrecisionSontAmbigues() {
        publiees(grille("A", source("FSE-UEMOA"), null, 10), grille("B", source("FSE-UEMOA"), null, 20));

        assertThatThrownBy(() -> calculer(unite("AG-KAYA"), null))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("GRILLE_AMBIGUE");
                    assertThat(e.getMessage()).contains("A v1", "B v1");
                });
    }

    @Test
    void grilleApplicableSansMontantAvecLesGrillesMoinsPrecisesEtLesVoisines() {
        publiees(grille("TOUTES", null, null, 10), grille("FSE", source("FSE-UEMOA"), null, 30),
                grille("OHG", source("AG-OUAHIGOUYA"), null, 40));
        when(grilles.compterVoisinesSource(any(), any(), any(), any(), any(), any())).thenReturn(2L);

        GrilleApplicable a = applicable(unite("SD-SAHEL-01"));
        assertThat(a.statut()).isEqualTo(GrilleApplicable.Statut.TROUVEE);
        assertThat(a.grille().code()).isEqualTo("OHG");
        assertThat(a.grille().source()).isEqualTo(new Correspondance(CibleReseau.Niveau.UNITE, "AG-OUAHIGOUYA", null));
        assertThat(a.autres()).extracting(GrilleApplicable.Retenue::code).containsExactly("FSE", "TOUTES");
        assertThat(a.voisinesSource()).isEqualTo(2);
        verify(grilles).compterVoisinesSource(eq("TRANSFERT"), eq("XOF"), any(), eq(reseau.id("MS-SAHEL")),
                eq("SOUS_DISTRIBUTEUR"), eq(reseau.id("SD-SAHEL-01")));

        assertThat(applicable(new Localisation(null, "BF", null)).voisinesSource())
                .as("un pays n'a pas de parent : pas de voisines").isZero();

        publiees(grille("KAYA", source("AG-KAYA"), null, 10));
        GrilleApplicable aucune = applicable(unite("SD-SAHEL-01"));
        assertThat(aucune.statut()).isEqualTo(GrilleApplicable.Statut.AUCUNE);
        assertThat(aucune.grille()).isNull();

        publiees(grille("A", source("FSE-UEMOA"), null, 10), grille("B", source("FSE-UEMOA"), null, 20));
        GrilleApplicable ambigue = applicable(unite("AG-KAYA"));
        assertThat(ambigue.statut()).isEqualTo(GrilleApplicable.Statut.AMBIGUE);
        assertThat(ambigue.message()).contains("A v1", "B v1");
        assertThat(ambigue.autres()).hasSize(2);
    }

    @Test
    void grilleApplicableAccepteUnRegroupementCommeCibleDeConfiguration() {
        publiees(grille("TOUTES", null, null, 10), grille("FSE", source("FSE-UEMOA"), null, 30),
                grille("NORD", source("RG-AGENCES-NORD"), null, 35));

        GrilleApplicable a = applicable(unite("RG-AGENCES-NORD"));
        assertThat(a.statut()).isEqualTo(GrilleApplicable.Statut.TROUVEE);
        assertThat(a.grille().code()).isEqualTo("NORD");
        assertThat(a.grille().source())
                .as("cible exacte du regroupement, pas une couverture via un membre")
                .isEqualTo(new Correspondance(CibleReseau.Niveau.UNITE, "RG-AGENCES-NORD", null));
        assertThat(a.autres()).extracting(GrilleApplicable.Retenue::code).containsExactly("FSE", "TOUTES");
    }

    private GrilleApplicable applicable(Localisation source) {
        return moteur.applicable(new DemandeFrais("TRANSFERT", null, "XOF", null, null, source, null));
    }

    private ResultatFrais calculer(Localisation source, Localisation destination) {
        return moteur.calculer(new DemandeFrais("TRANSFERT", null, "XOF", new BigDecimal("1000"), null,
                source, destination));
    }

    private Localisation unite(String code) {
        return new Localisation(reseau.id(code), null, null);
    }

    private CibleReseau source(String code) {
        return reseau.cible(code);
    }

    /** Le dépôt simulé renvoie toutes les grilles : le filtrage SQL est couvert par le test PostgreSQL. */
    private void publiees(GrilleTarifaire... liste) {
        when(grilles.candidates(any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(liste));
        when(grilles.compterApplicables(any(), any(), any(), any())).thenReturn((long) liste.length);
        when(grilles.compterCibleesSource(any(), any(), any(), any()))
                .thenReturn(Arrays.stream(liste).filter(g -> !CibleReseau.estVide(g.getSource())).count());
        when(grilles.compterCibleesDestination(any(), any(), any(), any()))
                .thenReturn(Arrays.stream(liste).filter(g -> !CibleReseau.estVide(g.getDestination())).count());
    }

    private static GrilleTarifaire grille(String code, CibleReseau source, CibleReseau destination, int frais) {
        GrilleTarifaire g = JeuDeDonnees.grille(code, "TRANSFERT", null, "XOF");
        g.setSource(source);
        g.setDestination(destination);
        g.ajouterTranche(JeuDeDonnees.tranche(1, "0", null, JeuDeDonnees.fixed(), Map.of("montantFixe", frais)));
        return g;
    }
}
