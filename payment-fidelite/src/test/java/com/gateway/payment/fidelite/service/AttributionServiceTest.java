package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.fidelite.domain.Adhesion;
import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.domain.InboundEvent;
import com.gateway.payment.fidelite.domain.LoyaltyRule;
import com.gateway.payment.fidelite.domain.LoyaltyRuleVersion;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.domain.Tenant;
import com.gateway.payment.fidelite.repository.AdhesionRepository;
import com.gateway.payment.fidelite.repository.InboundEventRepository;
import com.gateway.payment.fidelite.repository.LoyaltyRuleRepository;
import com.gateway.payment.fidelite.repository.LoyaltyRuleVersionRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import com.gateway.payment.fidelite.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttributionServiceTest {

    private static final String REGLE = """
            {"eventType":"PAYMENT_COMPLETED","calculation":{"type":"FIXED","points":40}}
            """;

    @Mock TenantRepository tenants;
    @Mock InboundEventRepository inbox;
    @Mock AdhesionRepository adhesions;
    @Mock ProgrammeRepository programmes;
    @Mock LoyaltyRuleRepository rules;
    @Mock LoyaltyRuleVersionRepository versions;
    @Mock LedgerService ledger;
    @Mock ReferralQualificationService referrals;

    AttributionService service;

    @BeforeEach
    void setUp() {
        service = new AttributionService(
                tenants, inbox, adhesions, programmes, rules, versions, ledger, new RuleEngine(), new ObjectMapper(),
                referrals);
    }

    @Test
    void simulationDoesNotTouchTheLedger() {
        Evenement event = payment(UUID.randomUUID());

        RuleEngine.Outcome outcome = service.simuler(REGLE, event);

        assertThat(outcome.points()).isEqualTo(40);
        verifyNoInteractions(ledger, inbox, adhesions);
    }

    @Test
    void theSameEventCreditsOnce() {
        UUID partyId = UUID.randomUUID();
        UUID programmeId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Tenant tenant = tenant();
        Programme programme = programme(programmeId, "CORIS_DEMO");
        Adhesion adhesion = new Adhesion();
        adhesion.setProgramme(programme);
        adhesion.setPartyId(partyId);
        LoyaltyRuleVersion version = version(programme);
        when(tenants.findByCode("CORIS_DEMO")).thenReturn(Optional.of(tenant));
        when(inbox.findByTenantIdAndEventId("CORIS_DEMO", eventId)).thenReturn(Optional.empty(), Optional.of(stored()));
        when(inbox.save(any())).thenAnswer(invocation -> stored(invocation.getArgument(0)));
        when(adhesions.findByPartyId(partyId)).thenReturn(List.of(adhesion));
        when(versions.published("CORIS_DEMO", programmeId)).thenReturn(List.of(version));

        Evenement event = new Evenement(eventId, "PAYMENT_COMPLETED", partyId.toString(), new BigDecimal("25000"), "XOF", Map.of());
        AttributionService.Accuse first = service.recevoir("CORIS_DEMO", event);
        AttributionService.Accuse second = service.recevoir("CORIS_DEMO", event);

        assertThat(first.status()).isEqualTo("ACCEPTED");
        assertThat(second.status()).isEqualTo("ACCEPTED");
        verify(ledger, times(1)).crediter(eq("CORIS_DEMO"), eq(programmeId), eq(partyId), eq(40L), any(), any());
    }

    @Test
    void anAdhesionFromAnotherTenantIsIgnored() {
        UUID partyId = UUID.randomUUID();
        Programme other = programme(UUID.randomUUID(), "AUTRE");
        Adhesion adhesion = new Adhesion();
        adhesion.setProgramme(other);
        adhesion.setPartyId(partyId);
        when(tenants.findByCode("CORIS_DEMO")).thenReturn(Optional.of(tenant()));
        when(inbox.findByTenantIdAndEventId(eq("CORIS_DEMO"), any())).thenReturn(Optional.empty());
        when(inbox.save(any())).thenAnswer(invocation -> stored(invocation.getArgument(0)));
        when(adhesions.findByPartyId(partyId)).thenReturn(List.of(adhesion));

        service.recevoir("CORIS_DEMO", payment(partyId));

        verify(ledger, never()).crediter(any(), any(), any(), anyLong(), any(), any());
        verify(versions, never()).published(any(), any());
    }

    private static Tenant tenant() {
        Tenant tenant = new Tenant();
        tenant.setCode("CORIS_DEMO");
        tenant.setStatus("ACTIF");
        return tenant;
    }

    private static Programme programme(UUID id, String tenant) {
        Programme programme = new Programme();
        programme.setId(id);
        programme.setTenantId(tenant);
        programme.setStatut(ProgrammeStatut.ACTIF);
        return programme;
    }

    private static LoyaltyRuleVersion version(Programme programme) {
        LoyaltyRule rule = new LoyaltyRule();
        rule.setId(UUID.randomUUID());
        rule.setCode("BIENVENUE");
        rule.setProgramme(programme);
        rule.setTenantId(programme.getTenantId());
        LoyaltyRuleVersion version = new LoyaltyRuleVersion();
        version.setId(UUID.randomUUID());
        version.setRule(rule);
        version.setDefinition(REGLE);
        return version;
    }

    private static InboundEvent stored() {
        return stored(new InboundEvent());
    }

    private static InboundEvent stored(InboundEvent event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        return event;
    }

    private static Evenement payment(UUID partyId) {
        return new Evenement(
                UUID.randomUUID(),
                "PAYMENT_COMPLETED",
                partyId.toString(),
                new BigDecimal("1000"),
                "XOF",
                Map.of());
    }
}
