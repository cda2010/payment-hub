package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Evenement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivationCalculTest {

    private final RuleEngine engine = new RuleEngine();

    @Test
    void fefoTakesTheSoonestExpiryFirst() {
        Instant tot = Instant.parse("2026-12-01T00:00:00Z");
        Instant tard = Instant.parse("2027-06-01T00:00:00Z");
        UUID proche = UUID.randomUUID();
        UUID loin = UUID.randomUUID();
        UUID illimite = UUID.randomUUID();
        List<Fefo.Prise> prises = Fefo.prelever(List.of(
                new Fefo.Lot(illimite, 100, null, tot),
                new Fefo.Lot(loin, 40, tard, tot),
                new Fefo.Lot(proche, 30, tot, tot)
        ), 50);

        assertThat(prises).containsExactly(
                new Fefo.Prise(proche, 30),
                new Fefo.Prise(loin, 20));
    }

    @Test
    void aWeekendCampaignMatchesOnlySaturdayAndSunday() {
        Evenement event = new Evenement(UUID.randomUUID(), "PAYMENT_COMPLETED", UUID.randomUUID().toString(),
                new BigDecimal("1000"), "XOF", Map.of("channel", "QR"));
        Instant samedi = LocalDate.of(2026, 11, 21).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant mardi = LocalDate.of(2026, 11, 17).atStartOfDay().toInstant(ZoneOffset.UTC);

        assertThat(CampagneFiltre.correspond(true, samedi.minusSeconds(3600), samedi.plusSeconds(86400),
                true, null, null, null, null, null, "QR", null, event, samedi)).isTrue();
        assertThat(CampagneFiltre.correspond(true, mardi.minusSeconds(3600), mardi.plusSeconds(86400),
                true, null, null, null, null, null, "QR", null, event, mardi)).isFalse();
        assertThat(CampagneFiltre.bonus(100, new BigDecimal("2"), 50)).isEqualTo(150);
    }

    @Test
    void segmentsSeparateActiveInactiveAndQr() {
        Instant maintenant = Instant.parse("2026-11-18T00:00:00Z");
        Set<String> actif = SegmentCalcul.calculer(List.of(
                new SegmentCalcul.Activite(maintenant.minusSeconds(86400), new BigDecimal("1000"), "QR", "CASH_IN", "PAIEMENT")
        ), maintenant);
        Set<String> absent = SegmentCalcul.calculer(List.of(
                new SegmentCalcul.Activite(maintenant.minusSeconds(40L * 86400), new BigDecimal("1000"), "MOBILE", "CASH_IN", "PAIEMENT"),
                new SegmentCalcul.Activite(maintenant.minusSeconds(50L * 86400), new BigDecimal("200000"), "MOBILE", "CASH_IN", "PAIEMENT")
        ), maintenant);

        assertThat(actif).contains("ACTIF", "NOUVEAU", "QR");
        assertThat(absent).contains("INACTIF_30", "ATTRITION");
        assertThat(absent).doesNotContain("ACTIF");
    }

    @Test
    void aMultiplierAndATierBonusStack() {
        String rule = """
                {"eventType":"PAYMENT_COMPLETED","calculation":{"type":"FIXED","points":100,"multiplier":2},
                 "bonus":[{"when":{"operator":"EQ","field":"palier","value":"GOLD"},"points":50}]}
                """;
        Evenement event = new Evenement(UUID.randomUUID(), "PAYMENT_COMPLETED", UUID.randomUUID().toString(),
                new BigDecimal("1000"), "XOF", Map.of("palier", "GOLD"));

        assertThat(engine.evaluate(rule, event).points()).isEqualTo(250);
    }

    @Test
    void fiveEventsInTheWindowGrantTheBonusOncePerWindow() {
        String rule = """
                {"calculation":{"type":"WINDOW","metric":"COUNT","windowDays":7,"threshold":5,"points":80}}
                """;
        List<Evenement> histoire = new java.util.ArrayList<>();
        RuleEngine.Outcome avant = null;
        for (int i = 0; i < 5; i++) {
            Evenement event = new Evenement(UUID.randomUUID(), "PAYMENT_COMPLETED", "client",
                    BigDecimal.ONE, "XOF", Map.of());
            histoire.add(event);
            avant = FenetreCalcul.evaluer(rule, event, histoire);
        }

        assertThat(avant.applicable()).isTrue();
        assertThat(avant.points()).isEqualTo(80);
        assertThat(avant.motif()).startsWith("REF:WINDOW:");
    }
}
