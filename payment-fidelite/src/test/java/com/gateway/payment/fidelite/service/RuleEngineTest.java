package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Evenement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    private static final String PAIEMENT = """
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
    void twentyFiveThousandOnMobileEarnsFiftyPoints() {
        RuleEngine.Outcome outcome = engine.evaluate(PAIEMENT, payment(new BigDecimal("25000"), "MOBILE"));

        assertThat(outcome.applicable()).isTrue();
        assertThat(outcome.points()).isEqualTo(50);
    }

    @Test
    void anotherChannelEarnsNothing() {
        RuleEngine.Outcome outcome = engine.evaluate(PAIEMENT, payment(new BigDecimal("25000"), "AGENCE"));

        assertThat(outcome.applicable()).isFalse();
        assertThat(outcome.points()).isZero();
    }

    @Test
    void anEmptyProductListEarnsNothing() {
        String rule = PAIEMENT.replace(
                "{\"operator\": \"EQ\", \"field\": \"channel\", \"value\": \"MOBILE\"}",
                "{\"operator\": \"EQ\", \"field\": \"channel\", \"value\": \"MOBILE\"}, {\"operator\": \"IN\", \"field\": \"productCode\", \"values\": []}");
        Evenement event = new Evenement(
                UUID.randomUUID(),
                "PAYMENT_COMPLETED",
                UUID.randomUUID().toString(),
                new BigDecimal("25000"),
                "XOF",
                Map.of("channel", "MOBILE", "productCode", "CASH_IN"));

        assertThat(engine.evaluate(rule, event).applicable()).isFalse();
    }

    @Test
    void aListedProductOfAMerchantUnderTheChosenCountryEarnsPoints() {
        String rule = PAIEMENT.replace(
                "\"limits\"",
                "\"unite\": {\"id\": \"pays-bf\", \"type\": \"PAYS\"}, \"limits\"")
                .replace(
                        "{\"operator\": \"EQ\", \"field\": \"channel\", \"value\": \"MOBILE\"}",
                        "{\"operator\": \"EQ\", \"field\": \"channel\", \"value\": \"MOBILE\"}, {\"operator\": \"IN\", \"field\": \"productCode\", \"values\": [\"CASH_IN\"]}");
        Evenement event = new Evenement(
                UUID.randomUUID(),
                "PAYMENT_COMPLETED",
                UUID.randomUUID().toString(),
                new BigDecimal("25000"),
                "XOF",
                Map.of(
                        "channel", "MOBILE",
                        "productCode", "CASH_IN",
                        "destinationUniteId", "marchand-1",
                        "destinationUniteType", "MARCHAND",
                        "lignee", List.of(
                                Map.of("id", "marchand-1", "type", "MARCHAND"),
                                Map.of("id", "pays-bf", "type", "PAYS"))));

        assertThat(engine.evaluate(rule, event).points()).isEqualTo(50);
    }

    @Test
    void anotherMerchantDoesNotEarnWhenTheRuleNamesOne() {
        String rule = PAIEMENT.replace(
                "\"limits\"",
                "\"unite\": {\"id\": \"marchand-1\", \"type\": \"MARCHAND\"}, \"limits\"");
        Evenement event = new Evenement(
                UUID.randomUUID(),
                "PAYMENT_COMPLETED",
                UUID.randomUUID().toString(),
                new BigDecimal("25000"),
                "XOF",
                Map.of(
                        "channel", "MOBILE",
                        "destinationUniteId", "marchand-2",
                        "destinationUniteType", "MARCHAND",
                        "lignee", List.of(Map.of("id", "marchand-2", "type", "MARCHAND"))));

        assertThat(engine.evaluate(rule, event).applicable()).isFalse();
    }

    @Test
    void theTransactionCapStopsTheGrant() {
        RuleEngine.Outcome outcome = engine.evaluate(PAIEMENT, payment(new BigDecimal("1000000"), "MOBILE"));

        assertThat(outcome.points()).isEqualTo(500);
    }

    private static Evenement payment(BigDecimal amount, String channel) {
        return new Evenement(
                UUID.randomUUID(),
                "PAYMENT_COMPLETED",
                UUID.randomUUID().toString(),
                amount,
                "XOF",
                Map.of("channel", channel));
    }
}
