package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.IdentityDtos.PartyRequest;
import com.gateway.payment.identity.domain.IdentityParty;
import com.gateway.payment.identity.domain.PartyKind;
import com.gateway.payment.identity.repository.IdentityChannelUserRepository;
import com.gateway.payment.identity.repository.IdentityMembershipRepository;
import com.gateway.payment.identity.repository.IdentityOrgNodeRepository;
import com.gateway.payment.identity.repository.IdentityPartyProfileRepository;
import com.gateway.payment.identity.repository.IdentityPartyRepository;
import com.gateway.payment.identity.repository.IdentityProfileGridRepository;
import com.gateway.payment.identity.repository.IdentityProfileLimitRepository;
import com.gateway.payment.identity.repository.IdentityProfileRepository;
import com.gateway.payment.identity.repository.IdentityProfileServiceRepository;
import com.gateway.payment.identity.repository.IdentityUniteProfileRepository;
import com.gateway.payment.inbox.kafka.PartyActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycRenewInboxTest {

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
    @Mock com.gateway.payment.inbox.kafka.PartyActionPublisher actions;

    IdentityService service;

    final UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");

    @BeforeEach
    void setUp() {
        service = new IdentityService(parties, orgs, users, memberships,
                profiles, profileServices, profileGrids, profileLimits, partyProfiles, uniteProfiles, actions);
    }

    @Test
    void basicKycOpensRenewal() {
        when(parties.findByTenantIdAndCode("CORIS_DEMO", "PERS-NEW")).thenReturn(Optional.empty());
        when(parties.save(any())).thenAnswer(invocation -> {
            IdentityParty party = invocation.getArgument(0);
            party.setId(partyId);
            return party;
        });

        service.createParty(new PartyRequest(
                "CORIS_DEMO", "PERS-NEW", PartyKind.PERSON, "Awa",
                null, null, null, "BASIC", null, true));

        ArgumentCaptor<PartyActionEvent> event = ArgumentCaptor.forClass(PartyActionEvent.class);
        verify(actions).publish(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo(PartyActionEvent.OPEN);
        assertThat(event.getValue().actionType()).isEqualTo("KYC_RENEW");
        assertThat(event.getValue().partyId()).isEqualTo(partyId);
    }

    @Test
    void raisedKycClosesRenewal() {
        IdentityParty party = IdentityParty.builder()
                .id(partyId)
                .tenantId("CORIS_DEMO")
                .code("PERS-NEW")
                .kind(PartyKind.PERSON)
                .displayName("Awa")
                .kycLevel("BASIC")
                .active(true)
                .build();
        when(parties.findById(partyId)).thenReturn(Optional.of(party));
        when(parties.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateParty(partyId, new PartyRequest(
                "CORIS_DEMO", "PERS-NEW", PartyKind.PERSON, "Awa",
                null, null, null, "STANDARD", null, true));

        ArgumentCaptor<PartyActionEvent> event = ArgumentCaptor.forClass(PartyActionEvent.class);
        verify(actions).publish(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo(PartyActionEvent.COMPLETE);
        assertThat(event.getValue().actionType()).isEqualTo("KYC_RENEW");
    }
}
