package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.IdentityDtos.*;
import com.gateway.payment.identity.domain.*;
import com.gateway.payment.identity.repository.*;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilOffreTest {

    @Mock IdentityPartyRepository parties;
    @Mock IdentityOrgNodeRepository orgs;
    @Mock IdentityChannelUserRepository users;
    @Mock IdentityMembershipRepository memberships;
    @Mock IdentityProfileRepository profiles;
    @Mock IdentityProfileServiceRepository profileServices;
    @Mock IdentityProfileGridRepository profileGrids;
    @Mock IdentityProfileLimitRepository profileLimits;
    @Mock IdentityPartyProfileRepository partyProfiles;
    @Mock IdentityUniteProfileRepository uniteProfiles;
    @Mock PartyActionPublisher actions;

    IdentityService service;

    final UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");
    final UUID uniteId = UUID.fromString("b1000001-0000-4000-8000-000000000010");
    final UUID profileId = UUID.fromString("f1000001-0000-4000-8000-000000000001");

    @BeforeEach
    void setUp() {
        service = new IdentityService(parties, orgs, users, memberships,
                profiles, profileServices, profileGrids, profileLimits, partyProfiles, uniteProfiles, actions);
    }

    @Test
    void creerProfilService() {
        when(profiles.findByTenantIdAndCode("CORIS_DEMO", "AGENT-PLUS")).thenReturn(Optional.empty());
        when(profiles.save(any())).thenAnswer(inv -> {
            IdentityProfile p = inv.getArgument(0);
            p.setId(profileId);
            return p;
        });
        when(profileServices.findByProfileIdOrderByServiceCodeAsc(profileId)).thenReturn(List.of(
                IdentityProfileService.builder().profileId(profileId).serviceCode("CASH_IN").active(true).build()));
        when(profileGrids.findByProfileIdOrderByProduitCodeAsc(profileId)).thenReturn(List.of());

        ProfileDto dto = service.createProfile(new ProfileRequest(
                "CORIS_DEMO", "agent-plus", "Agent plus", null, ProfileKind.SERVICE, IdentityAudience.AGENT,
                List.of(new ProfileServiceDto("cash_in", "Dépôt", true)),
                null, null));

        assertThat(dto.code()).isEqualTo("AGENT-PLUS");
        assertThat(dto.kind()).isEqualTo(ProfileKind.SERVICE);
        assertThat(dto.services()).extracting(ProfileServiceDto::serviceCode).containsExactly("CASH_IN");
        assertThat(dto.grilles()).isEmpty();

        ArgumentCaptor<IdentityProfileService> svc = ArgumentCaptor.forClass(IdentityProfileService.class);
        verify(profileServices).save(svc.capture());
        assertThat(svc.getValue().getServiceCode()).isEqualTo("CASH_IN");
    }

    @Test
    void offreUnionServicesEtTarifsSepares() {
        IdentityParty party = IdentityParty.builder()
                .id(partyId).tenantId("CORIS_DEMO").code("PERS-RETAIL1")
                .kind(PartyKind.PERSON).displayName("Awa").active(true).build();
        when(parties.findById(partyId)).thenReturn(Optional.of(party));

        UUID svcId = profileId;
        UUID feeRecent = UUID.fromString("f1000001-0000-4000-8000-000000000102");
        UUID feeOld = UUID.fromString("f1000001-0000-4000-8000-000000000101");
        when(partyProfiles.findByPartyIdAndActiveTrueOrderByDateDebutDesc(partyId)).thenReturn(List.of(
                IdentityPartyProfile.builder().partyId(partyId).profileId(feeRecent)
                        .dateDebut(LocalDate.of(2026, 6, 1)).active(true).build(),
                IdentityPartyProfile.builder().partyId(partyId).profileId(svcId)
                        .dateDebut(LocalDate.of(2026, 1, 1)).active(true).build(),
                IdentityPartyProfile.builder().partyId(partyId).profileId(feeOld)
                        .dateDebut(LocalDate.of(2026, 1, 1)).active(true).build()));
        when(profiles.findByIdIn(anyList())).thenReturn(List.of(
                IdentityProfile.builder().id(svcId).tenantId("CORIS_DEMO").code("RETAIL-STD")
                        .libelle("Retail svc").kind(ProfileKind.SERVICE).active(true).build(),
                IdentityProfile.builder().id(feeRecent).tenantId("CORIS_DEMO").code("AGENT-FEE")
                        .libelle("Agent fee").kind(ProfileKind.TARIFF).active(true).build(),
                IdentityProfile.builder().id(feeOld).tenantId("CORIS_DEMO").code("RETAIL-FEE")
                        .libelle("Retail fee").kind(ProfileKind.TARIFF).active(true).build()));
        when(profileServices.findByProfileIdOrderByServiceCodeAsc(any())).thenReturn(List.of());
        when(profileGrids.findByProfileIdOrderByProduitCodeAsc(any())).thenReturn(List.of());
        when(profileServices.findByProfileIdInAndActiveTrue(anyList())).thenReturn(List.of(
                IdentityProfileService.builder().profileId(svcId).serviceCode("BILL_PAYMENT").active(true).build(),
                IdentityProfileService.builder().profileId(svcId).serviceCode("CASH_IN").active(true).build()));
        when(profileGrids.findByProfileIdIn(anyList())).thenReturn(List.of(
                IdentityProfileGrid.builder().profileId(feeOld).produitCode("BILL_PAYMENT")
                        .grilleCode("FEE-BILL").canalCode("").build(),
                IdentityProfileGrid.builder().profileId(feeRecent).produitCode("BILL_PAYMENT")
                        .grilleCode("FEE-BILL-AGENT").canalCode("").build()));

        OffreDto offre = service.offreParty(partyId, LocalDate.of(2026, 10, 16));

        assertThat(offre.services()).extracting(ProfileServiceDto::serviceCode)
                .containsExactlyInAnyOrder("BILL_PAYMENT", "CASH_IN");
        assertThat(offre.grilles()).hasSize(1);
        assertThat(offre.grilles().get(0).grilleCode())
                .as("profil TARIFF le plus récent l'emporte pour BILL_PAYMENT")
                .isEqualTo("FEE-BILL-AGENT");
    }

    @Test
    void affectationRefuseTenantDifferent() {
        when(parties.findById(partyId)).thenReturn(Optional.of(IdentityParty.builder()
                .id(partyId).tenantId("CORIS_DEMO").code("X").kind(PartyKind.PERSON)
                .displayName("X").active(true).build()));
        when(profiles.findById(profileId)).thenReturn(Optional.of(IdentityProfile.builder()
                .id(profileId).tenantId("AUTRE").code("P").libelle("P")
                .kind(ProfileKind.SERVICE).active(true).build()));

        assertThatThrownBy(() -> service.assignProfile(partyId, new PartyProfileRequest(profileId, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenant");
    }

    @Test
    void affecterEtResoudreOffreUnite() {
        when(profiles.findById(profileId)).thenReturn(Optional.of(IdentityProfile.builder()
                .id(profileId).tenantId("CORIS_DEMO").code("AGENT-STD").libelle("Agent")
                .kind(ProfileKind.SERVICE).active(true).build()));
        when(uniteProfiles.findByUniteIdAndProfileId(uniteId, profileId)).thenReturn(Optional.empty());
        when(uniteProfiles.save(any())).thenAnswer(inv -> {
            IdentityUniteProfile a = inv.getArgument(0);
            a.setId(UUID.fromString("c1000001-0000-4000-8000-000000000001"));
            return a;
        });
        when(profileServices.findByProfileIdOrderByServiceCodeAsc(profileId)).thenReturn(List.of());
        when(profileGrids.findByProfileIdOrderByProduitCodeAsc(profileId)).thenReturn(List.of());

        UniteProfileDto aff = service.assignUniteProfile(uniteId,
                new UniteProfileRequest(profileId, "CORIS_DEMO", null, null));
        assertThat(aff.uniteId()).isEqualTo(uniteId);
        assertThat(aff.profileCode()).isEqualTo("AGENT-STD");

        when(uniteProfiles.findByUniteIdAndActiveTrueOrderByDateDebutDesc(uniteId)).thenReturn(List.of(
                IdentityUniteProfile.builder().uniteId(uniteId).profileId(profileId)
                        .dateDebut(LocalDate.of(2026, 1, 1)).active(true).build()));
        when(profiles.findByIdIn(anyList())).thenReturn(List.of(
                IdentityProfile.builder().id(profileId).tenantId("CORIS_DEMO").code("AGENT-STD")
                        .libelle("Agent").active(true).build()));
        when(profileServices.findByProfileIdInAndActiveTrue(anyList())).thenReturn(List.of(
                IdentityProfileService.builder().profileId(profileId).serviceCode("CASH_IN").active(true).build()));
        when(profileGrids.findByProfileIdIn(anyList())).thenReturn(List.of(
                IdentityProfileGrid.builder().profileId(profileId).produitCode("CASH_IN")
                        .grilleCode("FEE-CASH-AGENT").canalCode("").build()));

        OffreDto offre = service.offreUnite(uniteId, LocalDate.of(2026, 10, 16));
        assertThat(offre.ownerKind()).isEqualTo("UNITE");
        assertThat(offre.ownerId()).isEqualTo(uniteId);
        assertThat(offre.services()).extracting(ProfileServiceDto::serviceCode).containsExactly("CASH_IN");
        assertThat(offre.grilles()).extracting(ProfileGridDto::grilleCode).containsExactly("FEE-CASH-AGENT");
    }

    @Test
    void creerProfilLimite() {
        when(profiles.findByTenantIdAndCode("CORIS_DEMO", "RETAIL-LEGER")).thenReturn(Optional.empty());
        when(profiles.save(any())).thenAnswer(inv -> {
            IdentityProfile p = inv.getArgument(0);
            p.setId(profileId);
            return p;
        });
        when(profileServices.findByProfileIdOrderByServiceCodeAsc(profileId)).thenReturn(List.of());
        when(profileGrids.findByProfileIdOrderByProduitCodeAsc(profileId)).thenReturn(List.of());
        when(profileLimits.findByProfileIdOrderByProduitCodeAsc(profileId)).thenReturn(List.of(
                IdentityProfileLimit.builder().profileId(profileId).produitCode("CASH_OUT")
                        .sens(SensLimite.EMETTEUR).groupeCode("CASH_OUT_LEGER").build()));

        ProfileDto dto = service.createProfile(new ProfileRequest(
                "CORIS_DEMO", "retail-leger", "Retail léger", null, ProfileKind.LIMIT, IdentityAudience.RETAIL,
                null, null,
                List.of(new ProfileLimitDto("cash_out", SensLimite.EMETTEUR, "cash_out_leger"))));

        assertThat(dto.kind()).isEqualTo(ProfileKind.LIMIT);
        assertThat(dto.services()).isEmpty();
        assertThat(dto.grilles()).isEmpty();
        assertThat(dto.limites()).extracting(ProfileLimitDto::groupeCode).containsExactly("CASH_OUT_LEGER");

        ArgumentCaptor<IdentityProfileLimit> ligne = ArgumentCaptor.forClass(IdentityProfileLimit.class);
        verify(profileLimits).save(ligne.capture());
        assertThat(ligne.getValue().getProduitCode()).isEqualTo("CASH_OUT");
        assertThat(ligne.getValue().getSens()).isEqualTo(SensLimite.EMETTEUR);
        assertThat(ligne.getValue().getGroupeCode()).isEqualTo("CASH_OUT_LEGER");
    }

    @Test
    void profilLimiteRefuseUnService() {
        when(profiles.findByTenantIdAndCode("CORIS_DEMO", "RETAIL-LEGER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProfile(new ProfileRequest(
                "CORIS_DEMO", "retail-leger", "Retail léger", null, ProfileKind.LIMIT, IdentityAudience.RETAIL,
                List.of(new ProfileServiceDto("CASH_IN", null, true)),
                null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LIMIT");
    }

    @Test
    void offreLimiteLaPlusRecenteEmporte() {
        IdentityParty party = IdentityParty.builder()
                .id(partyId).tenantId("CORIS_DEMO").code("PERS-RETAIL1")
                .kind(PartyKind.PERSON).displayName("Awa").active(true).build();
        when(parties.findById(partyId)).thenReturn(Optional.of(party));

        UUID recent = UUID.fromString("f1000001-0000-4000-8000-000000000202");
        UUID ancien = UUID.fromString("f1000001-0000-4000-8000-000000000201");
        when(partyProfiles.findByPartyIdAndActiveTrueOrderByDateDebutDesc(partyId)).thenReturn(List.of(
                IdentityPartyProfile.builder().partyId(partyId).profileId(recent)
                        .dateDebut(LocalDate.of(2026, 6, 1)).active(true).build(),
                IdentityPartyProfile.builder().partyId(partyId).profileId(ancien)
                        .dateDebut(LocalDate.of(2026, 1, 1)).active(true).build()));
        when(profiles.findByIdIn(anyList())).thenReturn(List.of(
                IdentityProfile.builder().id(recent).tenantId("CORIS_DEMO").code("RETAIL-IDENTIFIE")
                        .libelle("Identifié").kind(ProfileKind.LIMIT).active(true).build(),
                IdentityProfile.builder().id(ancien).tenantId("CORIS_DEMO").code("RETAIL-LEGER")
                        .libelle("Léger").kind(ProfileKind.LIMIT).active(true).build()));
        when(profileServices.findByProfileIdOrderByServiceCodeAsc(any())).thenReturn(List.of());
        when(profileGrids.findByProfileIdOrderByProduitCodeAsc(any())).thenReturn(List.of());
        when(profileLimits.findByProfileIdOrderByProduitCodeAsc(any())).thenReturn(List.of());
        when(profileLimits.findByProfileIdIn(anyList())).thenReturn(List.of(
                IdentityProfileLimit.builder().profileId(ancien).produitCode("CASH_OUT")
                        .sens(SensLimite.EMETTEUR).groupeCode("CASH_OUT_LEGER").build(),
                IdentityProfileLimit.builder().profileId(ancien).produitCode("CASH_OUT")
                        .sens(SensLimite.DETENTION).groupeCode("DETENTION_LEGER").build(),
                IdentityProfileLimit.builder().profileId(recent).produitCode("CASH_OUT")
                        .sens(SensLimite.EMETTEUR).groupeCode("CASH_OUT_STANDARD").build()));

        OffreDto offre = service.offreParty(partyId, LocalDate.of(2026, 10, 16));

        assertThat(offre.limites()).extracting(ProfileLimitDto::groupeCode)
                .containsExactly("CASH_OUT_STANDARD", "DETENTION_LEGER");
    }
}
