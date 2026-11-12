package com.gateway.payment.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.i18n.GatewayI18nProperties;
import com.gateway.payment.common.i18n.GatewayI18nService;
import com.gateway.payment.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryLabelFormatterTest {

    @Test
    void transferTitle_includesPartyAndRail() {
        HistoryLabelFormatter formatter = formatter("fr");
        HistoryLabelFormatter.Labels labels = formatter.format(
                "ACCOUNT_TRANSFER",
                "TRANSFERT",
                "SUCCESS",
                "ORANGE_MONEY",
                Instant.parse("2026-09-16T10:42:00Z"),
                Map.of(
                        "beneficiaryName", "Aminata Traoré",
                        "destPhone", "70123456",
                        "destinationProvider", "ORANGE_MONEY"
                ),
                "Transfert account"
        );

        assertThat(labels.title()).isEqualTo("Transfert · vers Aminata Traoré (70 12 34 56) · Orange Money");
        assertThat(labels.subtitle()).startsWith("Validée");
        assertThat(labels.productLabel()).isEqualTo("Transfert account");
    }

    @Test
    void billTitle_includesBiller() {
        HistoryLabelFormatter.Labels labels = formatter("fr").format(
                "BILL_PAYMENT",
                "PAIEMENT_FACTURE",
                "SUCCESS",
                "SONABEL",
                Instant.parse("2026-09-05T09:08:00Z"),
                Map.of(
                        "billerName", "SONABEL",
                        "billReference", "12345678"
                ),
                null
        );

        assertThat(labels.title()).contains("Paiement de facture");
        assertThat(labels.title()).contains("vers SONABEL");
        assertThat(labels.productLabel()).isEqualTo("Paiement de facture");
    }

    @Test
    void englishLocale_switchesKindAndPreposition() {
        HistoryLabelFormatter.Labels labels = formatter("en").format(
                "CASH_OUT",
                null,
                "PROCESSING",
                "GAB",
                Instant.parse("2026-09-16T10:42:00Z"),
                Map.of("withdrawalChannel", "GAB"),
                null
        );

        assertThat(labels.title()).isEqualTo("Cash withdrawal · via ATM");
        assertThat(labels.subtitle()).startsWith("Processing");
        assertThat(labels.productLabel()).isEqualTo("Cash withdrawal");
    }

    @Test
    void cashOutTitle_includesAgentName() {
        HistoryLabelFormatter.Labels labels = formatter("fr").format(
                "CASH_OUT",
                null,
                "SUCCESS",
                "CORIS_MONEY",
                Instant.parse("2026-09-16T10:42:00Z"),
                Map.of(
                        "withdrawalChannel", "AGENT",
                        "agentUniteLibelle", "Agent Ouahigouya",
                        "agentCode", "AG-OUAHIGOUYA"
                ),
                "Cash-out"
        );

        assertThat(labels.title()).isEqualTo("Retrait d’espèces · via Agent Ouahigouya (AG-OUAHIGOUYA)");
        assertThat(labels.productLabel()).isEqualTo("Cash-out");
    }

    @Test
    void productHistoryTemplate_overridesTitle() {
        HistoryLabelFormatter.Labels labels = formatter("fr").format(
                "CASH_OUT",
                null,
                "SUCCESS",
                "CORIS_MONEY",
                Instant.parse("2026-09-16T10:42:00Z"),
                Map.of(
                        "withdrawalChannel", "AGENT",
                        "agentUniteLibelle", "Agent Ouahigouya",
                        "agentCode", "AG-OUAHIGOUYA",
                        "amount", 5000,
                        "currency", "XOF"
                ),
                "Cash-out",
                Map.of(
                        "kindLabel", "Retrait agent",
                        "titleTemplate", "{{kind}} chez {{agentDisplay}}",
                        "subtitleTemplate", "{{statusLabel}} · {{amount}} {{currency}}"
                )
        );

        assertThat(labels.title()).isEqualTo("Retrait agent chez Agent Ouahigouya (AG-OUAHIGOUYA)");
        assertThat(labels.subtitle()).isEqualTo("Validée · 5000 XOF");
    }

    private static HistoryLabelFormatter formatter(String lang) {
        return new HistoryLabelFormatter(new FixedLangI18n(lang), new TemplateMapper(new ObjectMapper()));
    }

    /** Force la langue hors requête HTTP (pas de RequestContextHolder en unitaire). */
    private static final class FixedLangI18n extends GatewayI18nService {
        private final String lang;

        FixedLangI18n(String lang) {
            super(new GatewayI18nProperties());
            this.lang = lang;
        }

        @Override
        public String currentLanguage() {
            return lang;
        }

        @Override
        public String resolveMessage(Map<String, String> messages) {
            return LocalizedMessages.resolve(messages, lang);
        }
    }
}
