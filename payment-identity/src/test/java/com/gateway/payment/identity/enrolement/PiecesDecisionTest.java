package com.gateway.payment.identity.enrolement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PiecesDecisionTest {

    private final PiecesDecision decision = new PiecesDecision(new EnrollmentProperties());

    @Test
    void passportAsksForThePageAndTheSelfie() {
        assertThat(faces("BF", "PASSPORT")).containsExactly("RECTO", "SELFIE");
        assertThat(decision.demander("GN", "PASSPORT").getFirst().libelle()).isEqualTo("Page du passeport");
    }

    @Test
    void guineaIdentityCardHasNoBack() {
        assertThat(faces("GN", "CNI")).containsExactly("RECTO", "SELFIE");
    }

    @Test
    void nationalIdAsksForBothSidesAndTheSelfie() {
        assertThat(faces("BF", "CNI")).containsExactly("RECTO", "VERSO", "SELFIE");
        assertThat(faces("SN", "CARTE_CONSULAIRE")).containsExactly("RECTO", "VERSO", "SELFIE");
    }

    private List<String> faces(String pays, String type) {
        return decision.demander(pays, type).stream().map(ParcoursPays.Document::face).toList();
    }
}
