package com.gateway.payment.catalog.capability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RulesPartyTest {

    @Test
    void destinationDistincteQuandLesComptesDifferent() {
        assertThat(RulesParty.separateReceiver(
                "DESTINATION", "CLI-A", "CLI-B", "0011000003090", "0011100119801590311"))
                .isTrue();
    }

    @Test
    void cashInResteLeClientQuandSeulLeCompteDestinationEstConnu() {
        assertThat(RulesParty.separateReceiver(
                "DESTINATION", null, null, null, "22673061292"))
                .isFalse();
    }

    @Test
    void sourceNeBasculePasSurLeBeneficiaire() {
        assertThat(RulesParty.separateReceiver(
                "SOURCE", "CLI-A", "CLI-B", "0011000003090", "0011100119801590311"))
                .isFalse();
    }

    @Test
    void memeCompteNestPasUnBeneficiaireDistinct() {
        assertThat(RulesParty.separateReceiver(
                "DESTINATION", "CLI-A", "CLI-A", "0011000003090", "0011000003090"))
                .isFalse();
    }
}
