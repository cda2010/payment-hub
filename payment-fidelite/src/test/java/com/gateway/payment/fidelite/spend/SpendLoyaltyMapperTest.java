package com.gateway.payment.fidelite.spend;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.service.RuleEngine;
import com.gateway.payment.spend.kafka.SpendEvent;
import com.gateway.payment.spend.kafka.UniteRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpendLoyaltyMapperTest {

    private static final String REGLE = """
            {
              "eventType": "PAYMENT_COMPLETED",
              "conditions": {
                "operator": "AND",
                "rules": [
                  {"operator": "GTE", "field": "amount", "value": 5000},
                  {"operator": "EQ", "field": "channel", "value": "MOBILE"}
                ]
              },
              "calculation": {
                "type": "PROPORTIONAL",
                "unitAmount": 1000,
                "pointsPerUnit": 2,
                "rounding": "FLOOR"
              },
              "limits": {"maxPointsPerTransaction": 500}
            }
            """;

    private final RuleEngine engine = new RuleEngine();

    @Test
    void aMobilePaymentOf25000EarnsFiftyPoints() {
        Evenement event = SpendLoyaltyMapper.toEvenement(spend("MOBILE", "25000"));

        assertThat(engine.evaluate(REGLE, event).points()).isEqualTo(50);
        assertThat(event.eventId()).isEqualTo(SpendLoyaltyMapper.eventId("PAY-FIDELITE-1"));
    }

    @Test
    void withoutAChannelTheRuleDoesNotApply() {
        Evenement event = SpendLoyaltyMapper.toEvenement(spend(null, "25000"));

        assertThat(engine.evaluate(REGLE, event).applicable()).isFalse();
    }

    @Test
    void theSamePaymentKeepsTheSameEventId() {
        assertThat(SpendLoyaltyMapper.eventId("PAY-FIDELITE-1"))
                .isEqualTo(SpendLoyaltyMapper.eventId("PAY-FIDELITE-1"));
    }

    private static SpendEvent spend(String channel, String amount) {
        return new SpendEvent(
                "PAY-FIDELITE-1",
                UUID.fromString("a1000001-0000-4000-8000-000000000006"),
                "CORIS_DEMO",
                new BigDecimal(amount),
                "XOF",
                "PAYEMENT",
                null,
                null,
                "Carrefour",
                Instant.parse("2026-11-10T14:40:00Z"),
                channel,
                "marchand-1",
                "MARCHAND",
                List.of(
                        new UniteRef("marchand-1", "MARCHAND"),
                        new UniteRef("pays-bf", "PAYS")));
    }
}
