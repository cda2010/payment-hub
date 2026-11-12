package com.gateway.payment.pricing.tarif.service;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.ReseauDeTest;
import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.CibleDto;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.CibleRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleRef;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleDetail;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.TrancheRequest;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.repository.FormuleRepository;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import com.gateway.payment.pricing.tarif.validation.GrilleValidator;
import com.gateway.payment.pricing.tarif.validation.ParametreValidator;
import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrilleServiceTest {

    private final ReseauDeTest reseau = new ReseauDeTest();
    private final GrilleTarifaireRepository grilles = mock(GrilleTarifaireRepository.class);
    private final FormuleRepository formules = mock(FormuleRepository.class);
    private final GrilleService service = new GrilleService(grilles, formules,
            new GrilleValidator(new ParametreValidator()), mock(MoteurFrais.class), reseau, Clock.systemUTC());

    @ParameterizedTest
    @EnumSource(value = StatutGrille.class, names = "BROUILLON", mode = EnumSource.Mode.EXCLUDE)
    void ta012_seulUnBrouillonEstModifiable(StatutGrille statut) {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.setStatut(statut);
        when(grilles.findById(g.getId())).thenReturn(Optional.of(g));

        assertThatThrownBy(() -> service.supprimerTranche(g.getId(), g.getTranches().get(0).getId(), "maker"))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("GRILLE_NON_MODIFIABLE");
                    assertThat(e.getStatus().value()).isEqualTo(409);
                    assertThat(e.getMessage()).contains("nouvelle version");
                });
        assertThat(g.getTranches()).hasSize(4);
    }

    @Test
    void ajoutDeTrancheRenumeroteParMontant() {
        GrilleTarifaire g = brouillon();
        when(formules.findByCodeAndVersion("FIXED", 1)).thenReturn(Optional.of(JeuDeDonnees.fixed()));

        GrilleDetail detail = service.ajouterTranche(g.getId(), new TrancheRequest(new BigDecimal("500001"), null,
                new FormuleRef("fixed", 1), Map.of("montantFixe", 3000), null, null), "maker");

        assertThat(detail.tranches()).extracting(t -> t.ordre() + ":" + t.montantMin())
                .containsExactly("1:0", "2:8001", "3:25001", "4:100001", "5:500001");
        assertThat(detail.updatedBy()).isEqualTo("maker");
    }

    @Test
    void ta006_trancheAvecParametreManquantRejeteeALEnregistrement() {
        GrilleTarifaire g = brouillon();
        when(formules.findByCodeAndVersion("EXCESS_AX_PLUS_Y", 1)).thenReturn(Optional.of(JeuDeDonnees.excess()));

        assertThatThrownBy(() -> service.ajouterTranche(g.getId(), new TrancheRequest(new BigDecimal("500001"), null,
                new FormuleRef("EXCESS_AX_PLUS_Y", 1), Map.of("seuil", 500000), null, null), "maker"))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("CONFIGURATION_INVALIDE");
                    assertThat(e.getDetails()).extracting(ValidationIssue::code, ValidationIssue::tranche)
                            .containsExactly(org.assertj.core.groups.Tuple.tuple("PARAM_OBLIGATOIRE_MANQUANT", 5));
                });
    }

    @Test
    void ta013_formuleInconnueRejetee() {
        GrilleTarifaire g = brouillon();
        when(formules.findByCodeAndVersion("MAGIC", 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ajouterTranche(g.getId(), new TrancheRequest(BigDecimal.ZERO, null,
                new FormuleRef("MAGIC", 1), Map.of(), null, null), "maker"))
                .isInstanceOfSatisfying(TarifException.class, e ->
                        assertThat(e.getDetails()).extracting(ValidationIssue::code).containsExactly("FORMULE_INCONNUE"));
    }

    @Test
    void validationRapporteLesChevauchementsSansModifier() {
        GrilleTarifaire g = brouillon();
        g.getTranches().get(1).setMontantMin(new BigDecimal("7000"));

        var rapport = service.valider(g.getId());

        assertThat(rapport.valide()).isFalse();
        assertThat(rapport.erreurs()).extracting(ValidationIssue::code).containsExactly("TRANCHES_CHEVAUCHEMENT");
        assertThat(g.getStatut()).isEqualTo(StatutGrille.BROUILLON);
    }

    @Test
    void nouvelleVersionCopieLaGrillePublieeEnBrouillon() {
        GrilleTarifaire publiee = JeuDeDonnees.trfStd();
        when(grilles.findById(publiee.getId())).thenReturn(Optional.of(publiee));
        when(grilles.findByCodeAndStatut("TRF_STD", StatutGrille.BROUILLON)).thenReturn(List.of());
        when(grilles.findFirstByCodeOrderByVersionDesc("TRF_STD")).thenReturn(Optional.of(publiee));

        GrilleDetail v2 = service.nouvelleVersion(publiee.getId(), "maker");

        assertThat(v2.version()).isEqualTo(2);
        assertThat(v2.statut()).isEqualTo(StatutGrille.BROUILLON);
        assertThat(v2.modifiable()).isTrue();
        assertThat(v2.createdBy()).isEqualTo("maker");
        assertThat(v2.tranches()).extracting(t -> t.ordre() + ":" + t.montantMin())
                .containsExactly("1:0", "2:8001", "3:25001", "4:100001");
        assertThat(publiee.getStatut()).isEqualTo(StatutGrille.PUBLIEE);
        verify(grilles).save(any(GrilleTarifaire.class));
    }

    @Test
    void nouvelleVersionReprendLeBrouillonEnCours() {
        GrilleTarifaire publiee = JeuDeDonnees.trfStd();
        GrilleTarifaire enCours = JeuDeDonnees.trfStd();
        enCours.setVersion(2);
        enCours.setStatut(StatutGrille.BROUILLON);
        when(grilles.findById(publiee.getId())).thenReturn(Optional.of(publiee));
        when(grilles.findByCodeAndStatut("TRF_STD", StatutGrille.BROUILLON)).thenReturn(List.of(enCours));

        GrilleDetail detail = service.nouvelleVersion(publiee.getId(), "maker");

        assertThat(detail.id()).isEqualTo(enCours.getId());
        verify(grilles, never()).save(any());
    }

    @Test
    void publicationRemplaceLaVersionPublieeDuMemeCode() {
        GrilleTarifaire v1 = JeuDeDonnees.trfStd();
        GrilleTarifaire v2 = brouillon();
        v2.setVersion(2);
        when(grilles.findByProduitAndDeviseAndStatut("TRANSFERT", "XOF", StatutGrille.PUBLIEE)).thenReturn(List.of(v1));
        when(grilles.findByCodeAndStatut("TRF_STD", StatutGrille.PUBLIEE)).thenReturn(List.of(v1));

        GrilleDetail detail = service.publier(v2.getId(), "checker");

        assertThat(detail.statut()).isEqualTo(StatutGrille.PUBLIEE);
        assertThat(detail.modifiable()).isFalse();
        assertThat(v1.getStatut()).isEqualTo(StatutGrille.DESACTIVEE);
    }

    @Test
    void publicationDifereeArreteLaVersionPrecedenteLaVeille() {
        GrilleTarifaire v1 = JeuDeDonnees.trfStd();
        GrilleTarifaire v2 = brouillon();
        v2.setVersion(2);
        v2.setDateDebut(LocalDate.of(2026, 11, 1));
        when(grilles.findByCodeAndStatut("TRF_STD", StatutGrille.PUBLIEE)).thenReturn(List.of(v1));

        service.publier(v2.getId(), "checker");

        assertThat(v1.getStatut()).isEqualTo(StatutGrille.PUBLIEE);
        assertThat(v1.getDateFin()).isEqualTo(LocalDate.of(2026, 10, 31));
    }

    @Test
    void publicationRefuseeSiLaGrilleEstIncoherente() {
        GrilleTarifaire g = brouillon();
        g.getTranches().get(1).setMontantMin(new BigDecimal("7000"));

        assertThatThrownBy(() -> service.publier(g.getId(), "checker"))
                .isInstanceOfSatisfying(TarifException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("CONFIGURATION_INVALIDE");
                    assertThat(e.getDetails()).extracting(ValidationIssue::code).containsExactly("TRANCHES_CHEVAUCHEMENT");
                });
        assertThat(g.getStatut()).isEqualTo(StatutGrille.BROUILLON);
    }

    @Test
    void publicationRefuseeSiUneAutreGrilleCouvreLeMemePerimetre() {
        GrilleTarifaire g = brouillon();
        GrilleTarifaire autre = JeuDeDonnees.grille("TRF_PROMO", "TRANSFERT", "MOBILE_MONEY", "XOF");
        when(grilles.findByProduitAndDeviseAndStatut("TRANSFERT", "XOF", StatutGrille.PUBLIEE)).thenReturn(List.of(autre));

        assertThatThrownBy(() -> service.publier(g.getId(), "checker"))
                .isInstanceOfSatisfying(TarifException.class, e ->
                        assertThat(e.getCode()).isEqualTo("GRILLE_PERIMETRE_OCCUPE"));
        assertThat(g.getStatut()).isEqualTo(StatutGrille.BROUILLON);
    }

    @Test
    void creationAvecUneUniteSourceEtUneZoneDestination() {
        GrilleDetail g = service.creer(requete("XOF", new CibleRequest(null, reseau.id("MS-SAHEL")),
                new CibleRequest("cemac", null)), "maker");

        assertThat(g.source()).isEqualTo(new CibleDto(CibleReseau.Niveau.UNITE, "UEMOA", reseau.id("MS-SAHEL"),
                "MS-SAHEL", "MS-SAHEL", "MASTER"));
        assertThat(g.destination()).isEqualTo(new CibleDto(CibleReseau.Niveau.ZONE, "CEMAC", null, null, null, null));
        assertThat(service.creer(requete("XOF", null, null), "maker").source().niveau())
                .isEqualTo(CibleReseau.Niveau.TOUTES);
    }

    @Test
    void unRegroupementPeutEtreVise() {
        GrilleDetail g = service.creer(requete("XOF", null, new CibleRequest(null, reseau.id("RG-AGENCES-NORD"))), "maker");

        assertThat(g.destination().uniteType()).isEqualTo("REGROUPEMENT");
    }

    @Test
    void ciblesInvalidesRefusees() {
        assertThatThrownBy(() -> service.creer(requete("XAF", new CibleRequest(null, reseau.id("BF")), null), "maker"))
                .isInstanceOfSatisfying(TarifException.class, e -> assertThat(e.getDetails())
                        .extracting(ValidationIssue::code, ValidationIssue::champ)
                        .containsExactly(org.assertj.core.groups.Tuple.tuple("CIBLE_DEVISE_INCOHERENTE", "devise")));
        assertThat(codeDErreur(requete("XOF", new CibleRequest(null, UUID.randomUUID()), null))).isEqualTo("CIBLE_INCONNUE");
        assertThat(codeDErreur(requete("XOF", new CibleRequest("ZZZ", null), null))).isEqualTo("CIBLE_INCONNUE");
        assertThat(codeDErreur(requete("XOF", new CibleRequest("CEMAC", reseau.id("BF")), null)))
                .isEqualTo("CIBLE_ZONE_INCOHERENTE");
        reseau.desactiver("AG-KAYA");
        assertThat(codeDErreur(requete("XOF", null, new CibleRequest(null, reseau.id("AG-KAYA")))))
                .isEqualTo("CIBLE_INACTIVE");
        reseau.indisponible = true;
        assertThatThrownBy(() -> service.creer(requete("XOF", new CibleRequest("UEMOA", null), null), "maker"))
                .hasFieldOrPropertyWithValue("code", "RESEAU_INDISPONIBLE");
    }

    @Test
    void lePerimetreComprendLaSourceEtLaDestination() {
        GrilleTarifaire g = brouillon();
        g.setSource(reseau.cible("BF"));
        GrilleTarifaire autre = JeuDeDonnees.grille("TRF_PROMO", "TRANSFERT", "MOBILE_MONEY", "XOF");
        when(grilles.findByProduitAndDeviseAndStatut("TRANSFERT", "XOF", StatutGrille.PUBLIEE)).thenReturn(List.of(autre));

        assertThat(service.publier(g.getId(), "checker").statut()).isEqualTo(StatutGrille.PUBLIEE);

        GrilleTarifaire h = brouillon();
        h.setCode("TRF_BF");
        h.setSource(reseau.cible("BF"));
        autre.setSource(reseau.cible("BF"));
        assertThatThrownBy(() -> service.publier(h.getId(), "checker"))
                .hasFieldOrPropertyWithValue("code", "GRILLE_PERIMETRE_OCCUPE");
    }

    @Test
    void publicationRefuseeSiLUniteViseeAEteDesactivee() {
        GrilleTarifaire g = brouillon();
        g.setDestination(reseau.cible("AG-KAYA"));
        reseau.desactiver("AG-KAYA");

        assertThatThrownBy(() -> service.publier(g.getId(), "checker"))
                .isInstanceOfSatisfying(TarifException.class, e -> assertThat(e.getDetails())
                        .extracting(ValidationIssue::code, ValidationIssue::champ)
                        .containsExactly(org.assertj.core.groups.Tuple.tuple("CIBLE_INACTIVE", "destination.uniteId")));
        assertThat(g.getStatut()).isEqualTo(StatutGrille.BROUILLON);
    }

    @Test
    void nouvelleVersionReprendLesCibles() {
        GrilleTarifaire publiee = JeuDeDonnees.trfStd();
        publiee.setSource(reseau.cible("FSE-UEMOA"));
        publiee.setDestination(CibleReseau.zone("CEMAC"));
        when(grilles.findById(publiee.getId())).thenReturn(Optional.of(publiee));
        when(grilles.findByCodeAndStatut("TRF_STD", StatutGrille.BROUILLON)).thenReturn(List.of());

        GrilleDetail v2 = service.nouvelleVersion(publiee.getId(), "maker");

        assertThat(v2.source().uniteCode()).isEqualTo("FSE-UEMOA");
        assertThat(v2.destination().zone()).isEqualTo("CEMAC");
    }

    private String codeDErreur(GrilleRequest req) {
        try {
            service.creer(req, "maker");
            return null;
        } catch (TarifException e) {
            return e.getDetails().isEmpty() ? e.getCode() : e.getDetails().get(0).code();
        }
    }

    private static GrilleRequest requete(String devise, CibleRequest source, CibleRequest destination) {
        return new GrilleRequest("TRF_" + UUID.randomUUID().toString().substring(0, 8), "Transfert", "TRANSFERT",
                null, devise, LocalDate.of(2026, 1, 1), null, true, 0, null, null, source, destination);
    }

    private GrilleTarifaire brouillon() {
        GrilleTarifaire g = JeuDeDonnees.trfStd();
        g.setStatut(StatutGrille.BROUILLON);
        when(grilles.findById(g.getId())).thenReturn(Optional.of(g));
        return g;
    }
}
