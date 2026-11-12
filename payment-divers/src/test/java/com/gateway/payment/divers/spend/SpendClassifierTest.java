package com.gateway.payment.divers.spend;

import com.gateway.payment.divers.repository.CategorieDepenseRepository;
import com.gateway.payment.spend.kafka.SpendEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpendClassifierTest {

    @Mock
    CategorieDepenseRepository categories;

    SpendClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new SpendClassifier(categories);
    }

    @Test
    void billWithoutHintGoesToFactures() {
        SpendEvent event = event("PAIEMENT_FACTURE", null, "SONABEL");

        assertThat(classifier.classify(event)).contains("factures");
    }

    @Test
    void knownHintWins() {
        when(categories.existsById("alimentation")).thenReturn(true);
        SpendEvent event = event("PAYEMENT", "alimentation", "CARREFOUR");

        assertThat(classifier.classify(event)).contains("alimentation");
    }

    @Test
    void transferIsNotASpend() {
        SpendEvent event = event("TRANSFERT", null, null);

        assertThat(classifier.classify(event)).isEmpty();
    }

    @Test
    void merchantWithoutHintGoesToAutre() {
        SpendEvent event = event("PAYEMENT", null, "QR");

        assertThat(classifier.classify(event)).contains("autre");
    }

    private static SpendEvent event(String type, String hint, String product) {
        return new SpendEvent(
                "PAY-1",
                UUID.randomUUID(),
                "CORIS_DEMO",
                new BigDecimal("1500"),
                "XOF",
                type,
                product,
                hint,
                "Commerce",
                Instant.parse("2026-11-10T12:00:00Z"),
                null,
                null,
                null,
                null);
    }
}
