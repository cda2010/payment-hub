package com.gateway.payment.catalog.journey;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogAmountPolicyTest {

    private static Map<String, Object> steps(String... steps) {
        return Map.of("steps", List.of(steps));
    }

    @Test
    void capture_amount_defers_wherever_it_sits() {
        // ACCOUNT_TRANSFER : le montant est demandé en troisième position.
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(
                steps("CAPTURE_DESTINATION", "VALIDATE_ACCOUNT", "CAPTURE_AMOUNT", "CONFIRM")))
                .isTrue();
    }

    @Test
    void discovery_step_defers_only_when_it_opens_the_journey() {
        // REMITTANCE_RECEIVE, LINK : le parcours s'ouvre sans montant connu.
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(steps("CAPTURE_REFERENCE", "CONFIRM"))).isTrue();
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(steps("SELECT_PROVIDER", "CONFIRM"))).isTrue();
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(steps("LOOKUP_BILL", "CONFIRM"))).isTrue();

        assertThat(CatalogAmountPolicy.amountDeferredAtStart(steps("CONFIRM", "LOOKUP_BILL"))).isFalse();
    }

    @Test
    void explicit_flag_wins_over_steps() {
        Map<String, Object> configuration = Map.of(
                "gateway", Map.of("optionalAmountAtStart", true),
                "steps", List.of("CONFIRM", "AUTHENTICATE"));

        assertThat(CatalogAmountPolicy.amountDeferredAtStart(configuration)).isTrue();
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(
                Map.of("gateway", Map.of("optional_amount_at_start", "true")))).isTrue();
    }

    @Test
    void amount_stays_required_without_declaration() {
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(null)).isFalse();
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(Map.of())).isFalse();
        assertThat(CatalogAmountPolicy.amountDeferredAtStart(steps("CONFIRM", "AUTHENTICATE", "DEBIT"))).isFalse();
    }
}
