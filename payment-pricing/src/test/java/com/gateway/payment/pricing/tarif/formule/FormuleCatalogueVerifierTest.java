package com.gateway.payment.pricing.tarif.formule;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.StatutFormule;
import com.gateway.payment.pricing.tarif.domain.TypeDonnee;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormuleCatalogueVerifierTest {

    private final FormulaRegistry registry = JeuDeDonnees.registry();

    @Test
    void catalogueDuSeedConformeAuxImplementations() {
        assertThat(FormuleCatalogueVerifier.ecarts(JeuDeDonnees.toutes(), registry)).isEmpty();
    }

    @Test
    void formuleActiveSansImplementationSignalee() {
        Formule v2 = JeuDeDonnees.fixed();
        v2.setVersion(2);

        assertThat(FormuleCatalogueVerifier.ecarts(List.of(v2), registry))
                .singleElement().asString().contains("FIXED v2", "aucune implémentation");
    }

    @Test
    void formuleInactiveSansImplementationIgnoree() {
        Formule v2 = JeuDeDonnees.fixed();
        v2.setVersion(2);
        v2.setStatut(StatutFormule.INACTIVE);

        assertThat(FormuleCatalogueVerifier.ecarts(List.of(v2), registry)).isEmpty();
    }

    @Test
    void schemaDifferentSignale() {
        Formule excess = JeuDeDonnees.excess();
        excess.getParametres().get(0).setTypeDonnee(TypeDonnee.INTEGER);

        assertThat(FormuleCatalogueVerifier.ecarts(List.of(excess), registry))
                .singleElement().asString().contains("EXCESS_AX_PLUS_Y v1", "paramètres");
    }
}
