package com.gateway.payment.divers.service;

import com.gateway.payment.divers.api.TontineDtos.CreateTontineRequest;
import com.gateway.payment.divers.domain.Tontine;
import com.gateway.payment.divers.domain.TontineFrequency;
import com.gateway.payment.divers.domain.TontineMember;
import com.gateway.payment.divers.domain.TontineStatus;
import com.gateway.payment.divers.repository.TontineMemberRepository;
import com.gateway.payment.divers.repository.TontineRepository;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TontineServiceTest {

    @Mock
    private TontineRepository tontines;

    @Mock
    private TontineMemberRepository members;

    @Mock
    private PartyActionPublisher actions;

    @InjectMocks
    private TontineService service;

    @Test
    void creatorIsTheFirstMemberAndAdmin() {
        when(tontines.existsByInviteCode(any())).thenReturn(false);
        when(tontines.save(any(Tontine.class))).thenAnswer(invocation -> {
            Tontine tontine = invocation.getArgument(0);
            tontine.setId(UUID.randomUUID());
            return tontine;
        });
        when(members.save(any(TontineMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(members.findByTontineIdOrderByTurnNoAsc(any())).thenAnswer(invocation -> {
            TontineMember member = new TontineMember();
            member.setId(UUID.randomUUID());
            member.setUsername("awa");
            member.setDisplayName("awa");
            member.setTurnNo(1);
            member.setAdminMember(true);
            return List.of(member);
        });

        var view = service.create("awa", null, new CreateTontineRequest("Solidarité", new BigDecimal("10000"), TontineFrequency.MENSUELLE));

        assertThat(view.round()).isEqualTo(1);
        assertThat(view.totalRounds()).isEqualTo(1);
        assertThat(view.role()).isEqualTo("admin");
        assertThat(view.inviteCode()).startsWith("TONT-");
        assertThat(view.frequency()).isEqualTo(TontineFrequency.MENSUELLE);
        assertThat(view.members().getFirst().self()).isTrue();
    }

    @Test
    void lastUnpaidMemberClosesTheRoundAndOpensTheNext() {
        Tontine tontine = tontine(1, TontineStatus.ACTIVE);
        TontineMember awa = member(tontine, "awa", 1, true);
        awa.setPaid(true);
        TontineMember jean = member(tontine, "jean", 2, false);
        when(members.findByTontineIdAndUsername(tontine.getId(), "jean")).thenReturn(Optional.of(jean));
        when(members.findByTontineIdOrderByTurnNoAsc(tontine.getId())).thenReturn(List.of(awa, jean));
        when(tontines.save(tontine)).thenReturn(tontine);

        var view = service.contribute("jean", null, tontine.getId());

        assertThat(tontine.getRoundNo()).isEqualTo(2);
        assertThat(tontine.getStatus()).isEqualTo(TontineStatus.ACTIVE);
        assertThat(awa.isPaid()).isFalse();
        assertThat(jean.isPaid()).isFalse();
        assertThat(view.nextBeneficiaryIndex()).isEqualTo(1);
    }

    @Test
    void payingTheLastRoundCompletesTheTontine() {
        Tontine tontine = tontine(2, TontineStatus.ACTIVE);
        TontineMember awa = member(tontine, "awa", 1, false);
        TontineMember jean = member(tontine, "jean", 2, true);
        jean.setPaid(true);
        when(members.findByTontineIdAndUsername(tontine.getId(), "awa")).thenReturn(Optional.of(awa));
        when(members.findByTontineIdOrderByTurnNoAsc(tontine.getId())).thenReturn(List.of(awa, jean));
        when(tontines.save(tontine)).thenReturn(tontine);

        var view = service.contribute("awa", null, tontine.getId());

        assertThat(tontine.getStatus()).isEqualTo(TontineStatus.COMPLETED);
        assertThat(tontine.getRoundNo()).isEqualTo(2);
        assertThat(view.myPaid()).isTrue();
    }

    @Test
    void secondContributionInTheSameRoundIsRejected() {
        Tontine tontine = tontine(1, TontineStatus.ACTIVE);
        TontineMember awa = member(tontine, "awa", 1, false);
        awa.setPaid(true);
        when(members.findByTontineIdAndUsername(tontine.getId(), "awa")).thenReturn(Optional.of(awa));

        assertThatThrownBy(() -> service.contribute("awa", null, tontine.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cotisation déjà enregistrée pour ce tour");
    }

    @Test
    void creatingATontineOpensTheDueContribution() {
        UUID partyId = UUID.randomUUID();
        when(tontines.existsByInviteCode(any())).thenReturn(false);
        when(tontines.save(any(Tontine.class))).thenAnswer(invocation -> {
            Tontine tontine = invocation.getArgument(0);
            tontine.setId(UUID.randomUUID());
            return tontine;
        });
        when(members.save(any(TontineMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(members.findByTontineIdOrderByTurnNoAsc(any())).thenAnswer(invocation -> {
            TontineMember member = new TontineMember();
            member.setId(UUID.randomUUID());
            member.setUsername("awa");
            member.setDisplayName("awa");
            member.setTurnNo(1);
            member.setAdminMember(true);
            return List.of(member);
        });

        service.create("awa", partyId, new CreateTontineRequest("Solidarité", new BigDecimal("10000"), TontineFrequency.MENSUELLE));

        verify(actions).open(eq(partyId), eq("COTISATION_DUE"), eq("tontine"), any(), eq("Solidarité"), any());
    }

    private static Tontine tontine(int round, TontineStatus status) {
        Tontine tontine = new Tontine();
        tontine.setId(UUID.randomUUID());
        tontine.setName("Bureau");
        tontine.setContribution(new BigDecimal("5000"));
        tontine.setFrequency(TontineFrequency.HEBDOMADAIRE);
        tontine.setRoundNo(round);
        tontine.setInviteCode("TONT-BURO");
        tontine.setStatus(status);
        tontine.setCreatedAt(Instant.parse("2026-11-09T00:00:00Z"));
        return tontine;
    }

    private static TontineMember member(Tontine tontine, String username, int turn, boolean admin) {
        TontineMember member = new TontineMember();
        member.setId(UUID.randomUUID());
        member.setTontine(tontine);
        member.setUsername(username);
        member.setDisplayName(username);
        member.setTurnNo(turn);
        member.setPaid(false);
        member.setAdminMember(admin);
        return member;
    }
}
