package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Adhesion;
import com.gateway.payment.fidelite.domain.Echange;
import com.gateway.payment.fidelite.domain.Recompense;
import com.gateway.payment.fidelite.domain.Palier;
import com.gateway.payment.fidelite.domain.PalierSaisie;
import com.gateway.payment.fidelite.domain.Parrainage;
import com.gateway.payment.fidelite.domain.ParrainageStatut;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammePortee;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.repository.AdhesionRepository;
import com.gateway.payment.fidelite.repository.EchangeRepository;
import com.gateway.payment.fidelite.repository.PalierRepository;
import com.gateway.payment.fidelite.repository.RecompenseRepository;
import com.gateway.payment.fidelite.repository.ParrainageRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import com.gateway.payment.fidelite.service.FideliteService.PalierView;
import com.gateway.payment.fidelite.service.FideliteService.CreerProgramme;
import com.gateway.payment.fidelite.service.FideliteService.ProgrammeView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FideliteServiceTest {

    @Mock ProgrammeRepository programmes;
    @Mock AdhesionRepository adhesions;
    @Mock ParrainageRepository parrainages;
    @Mock PalierRepository paliers;
    @Mock LedgerService ledger;
    @Mock RecompenseRepository recompenses;
    @Mock EchangeRepository echanges;
    @Mock ReferralQualificationService referrals;

    FideliteService service;
    final List<Programme> stock = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new FideliteService(
                programmes, adhesions, parrainages, paliers, recompenses, echanges, ledger, referrals);
        lenient().when(paliers.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(recompenses.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(programmes.save(any())).thenAnswer(invocation -> {
            Programme programme = invocation.getArgument(0);
            if (programme.getId() == null) {
                programme.setId(UUID.randomUUID());
            }
            stock.add(programme);
            return programme;
        });
    }

    @Test
    void threeScopes() {
        ProgrammeView tous = service.creer("CORIS_DEMO", new CreerProgramme(
                "Tous", ProgrammePortee.TOUS, null, null, Set.of("IGNORE"), 1, 100, 50, null));
        assertThat(tous.portee()).isEqualTo(ProgrammePortee.TOUS);
        assertThat(tous.profils()).isEmpty();
        assertThat(tous.paliers()).extracting(PalierView::label).containsExactly("Bronze", "Argent", "Or", "Platine");

        assertThatThrownBy(() -> service.creer("CORIS_DEMO", new CreerProgramme(
                "Profils", ProgrammePortee.PROFIL, null, null, Set.of(), 0, 0, 0, null)))
                .isInstanceOf(IllegalArgumentException.class);

        ProgrammeView profil = service.creer("CORIS_DEMO", new CreerProgramme(
                "Profils", ProgrammePortee.PROFIL, null, null, Set.of("retail-std"), 0, 0, 0, null));
        assertThat(profil.profils()).containsExactly("RETAIL-STD");

        UUID marchand = UUID.randomUUID();
        ProgrammeView unite = service.creer("CORIS_DEMO", new CreerProgramme(
                "Boutique", ProgrammePortee.UNITE, marchand, "Épicerie", Set.of(), 2, 200, 80, null));
        assertThat(unite.uniteId()).isEqualTo(marchand);
        assertThat(unite.statut()).isEqualTo(ProgrammeStatut.ACTIF);
    }

    @Test
    void clientSeesOpenAndOwnProfilePrograms() {
        service.creer("CORIS_DEMO", new CreerProgramme("Tous", ProgrammePortee.TOUS, null, null, Set.of(), 0, 0, 0, null));
        service.creer("CORIS_DEMO", new CreerProgramme("Agents", ProgrammePortee.PROFIL, null, null, Set.of("AGENT"), 0, 0, 0, null));
        service.creer("CORIS_DEMO", new CreerProgramme(
                "Boutique", ProgrammePortee.UNITE, UUID.randomUUID(), "Épicerie", Set.of(), 0, 0, 0, null));
        when(programmes.findByTenantIdAndStatutOrderByCreatedAtDesc("CORIS_DEMO", ProgrammeStatut.ACTIF))
                .thenReturn(List.copyOf(stock));

        List<ProgrammeView> seen = service.visibles("CORIS_DEMO", UUID.randomUUID(), Set.of("agent"));

        assertThat(seen).extracting(ProgrammeView::nom).containsExactly("Tous", "Agents");
    }

    @Test
    void merchantEnrollsItsCustomerAndReferralWaitsForQualification() {
        UUID marchand = UUID.randomUUID();
        UUID parrain = UUID.randomUUID();
        UUID filleul = UUID.randomUUID();
        ProgrammeView created = service.creer("CORIS_DEMO", new CreerProgramme(
                "Boutique", ProgrammePortee.UNITE, marchand, "Épicerie", Set.of(), 0, 200, 80, null));
        Programme programme = stock.getFirst();
        when(programmes.findById(created.id())).thenReturn(Optional.of(programme));
        when(adhesions.existsByProgramme_IdAndPartyId(created.id(), parrain)).thenReturn(false, true);
        when(adhesions.existsByProgramme_IdAndPartyId(created.id(), filleul)).thenReturn(false);
        when(parrainages.existsByCode(any())).thenReturn(false);
        when(parrainages.save(any())).thenAnswer(invocation -> {
            Parrainage parrainage = invocation.getArgument(0);
            parrainage.setId(UUID.randomUUID());
            return parrainage;
        });
        when(parrainages.findByCodeIgnoreCase("PAR-TEST")).thenAnswer(invocation -> {
            Parrainage parrainage = new Parrainage();
            parrainage.setId(UUID.randomUUID());
            parrainage.setProgramme(programme);
            parrainage.setParrainPartyId(parrain);
            parrainage.setCode("PAR-TEST");
            parrainage.setStatut(com.gateway.payment.fidelite.domain.ParrainageStatut.OUVERT);
            return Optional.of(parrainage);
        });

        assertThatThrownBy(() -> service.adherer(created.id(), filleul, Set.of(), false))
                .isInstanceOf(IllegalArgumentException.class);

        service.adherer(created.id(), parrain, Set.of(), true);
        verify(adhesions).save(any(Adhesion.class));

        var code = service.parrainer(created.id(), parrain);
        assertThat(code.code()).startsWith("PAR-");

        service.accepter("par-test", filleul, Set.of());

        ArgumentCaptor<Parrainage> saved = ArgumentCaptor.forClass(Parrainage.class);
        verify(parrainages, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        assertThat(saved.getAllValues())
                .anyMatch(row -> row.getStatut() == ParrainageStatut.ASSOCIE && filleul.equals(row.getFilleulPartyId()));
        verify(ledger, never()).crediter(any(), any(), any(), org.mockito.ArgumentMatchers.anyLong(), any(), any());
    }

    @Test
    void balanceOf3450IsArgent() {
        List<Palier> niveaux = List.of(
                palier("BRONZE", "Bronze", 0),
                palier("SILVER", "Argent", 2_000),
                palier("GOLD", "Or", 8_000),
                palier("PLATINUM", "Platine", 20_000));

        var situation = FideliteService.situation(UUID.randomUUID(), UUID.randomUUID(), 3_450, niveaux);

        assertThat(situation.palier().label()).isEqualTo("Argent");
        assertThat(situation.palierSuivant().label()).isEqualTo("Or");
        assertThat(situation.pointsPourSuivant()).isEqualTo(4_550);
    }

    @Test
    void customTiersReplaceTheStandardLadder() {
        ProgrammeView view = service.creer("CORIS_DEMO", new CreerProgramme(
                "Boutique", ProgrammePortee.TOUS, null, null, Set.of(), 0, 0, 0,
                List.of(new PalierSaisie("ACCUEIL", "Accueil", 0, new BigDecimal("1.00"), List.of("Bienvenue")))));

        assertThat(view.paliers()).extracting(PalierView::code).containsExactly("ACCUEIL");
    }

    @Test
    void exchangeDebitsTheBalanceAndRefusesAShortBalance() {
        UUID programmeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        Programme programme = new Programme();
        programme.setId(programmeId);
        programme.setStatut(ProgrammeStatut.ACTIF);
        Recompense reward = new Recompense();
        reward.setProgramme(programme);
        reward.setCode("RW-FEE");
        reward.setTitre("Transfert sans frais");
        reward.setCout(500);
        when(programmes.findById(programmeId)).thenReturn(Optional.of(programme));
        when(adhesions.existsByProgramme_IdAndPartyId(programmeId, partyId)).thenReturn(true);
        when(recompenses.findByProgramme_IdAndCodeIgnoreCase(programmeId, "rw-fee")).thenReturn(Optional.of(reward));
        when(ledger.consommer(any(), eq(programmeId), eq(partyId), eq(500L), eq("Transfert sans frais"), any()))
                .thenThrow(new IllegalArgumentException("Points insuffisants pour cette récompense."))
                .thenReturn(300L);
        when(echanges.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> service.echanger(programmeId, "rw-fee", partyId))
                .isInstanceOf(IllegalArgumentException.class);

        var exchanged = service.echanger(programmeId, "rw-fee", partyId);

        assertThat(exchanged.pointsRestants()).isEqualTo(300);
        verify(ledger, org.mockito.Mockito.times(2))
                .consommer(any(), eq(programmeId), eq(partyId), eq(500L), eq("Transfert sans frais"), any());
        verify(echanges).save(any(Echange.class));
    }

    private static Palier palier(String code, String label, int seuil) {
        Palier palier = new Palier();
        palier.setCode(code);
        palier.setLabel(label);
        palier.setSeuilPoints(seuil);
        palier.setPointsParMille(BigDecimal.ONE);
        palier.setAvantages(List.of());
        return palier;
    }
}
