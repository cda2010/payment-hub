package com.gateway.payment.network.service;

import com.gateway.payment.network.domain.TypeUnite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.gateway.payment.network.domain.TypeUnite.AGENCE;
import static com.gateway.payment.network.domain.TypeUnite.FSE;
import static com.gateway.payment.network.domain.TypeUnite.MARCHAND;
import static com.gateway.payment.network.domain.TypeUnite.MASTER;
import static com.gateway.payment.network.domain.TypeUnite.PAYS;
import static com.gateway.payment.network.domain.TypeUnite.REGROUPEMENT;
import static com.gateway.payment.network.domain.TypeUnite.SOUS_DISTRIBUTEUR;
import static org.assertj.core.api.Assertions.assertThat;

class RegleRattachementTest {

    @ParameterizedTest(name = "{1} sous {0} : {2}")
    @CsvSource({
            "PAYS, FSE, true",
            "FSE, AGENCE, true",
            "AGENCE, MASTER, true",
            "AGENCE, MARCHAND, true",
            "MASTER, SOUS_DISTRIBUTEUR, true",
            "PAYS, AGENCE, false",
            "FSE, MASTER, false",
            "FSE, PAYS, false",
            "AGENCE, SOUS_DISTRIBUTEUR, false",
            "MASTER, MARCHAND, false",
            "MARCHAND, SOUS_DISTRIBUTEUR, false",
            "SOUS_DISTRIBUTEUR, MARCHAND, false",
            "AGENCE, FSE, false",
            "PAYS, REGROUPEMENT, true",
            "FSE, REGROUPEMENT, true",
            "AGENCE, REGROUPEMENT, true",
            "MASTER, REGROUPEMENT, true",
            "MARCHAND, REGROUPEMENT, true",
            "SOUS_DISTRIBUTEUR, REGROUPEMENT, true",
            "REGROUPEMENT, AGENCE, false",
            "REGROUPEMENT, REGROUPEMENT, false"
    })
    void matrice(TypeUnite parent, TypeUnite enfant, boolean autorise) {
        assertThat(RegleRattachement.peutRattacher(parent, enfant)).isEqualTo(autorise);
    }

    @Test
    void seulLePaysEstRacineEtLeRegroupementNAccueilleRien() {
        assertThat(RegleRattachement.estRacine(PAYS)).isTrue();
        assertThat(RegleRattachement.estRacine(FSE)).isFalse();
        assertThat(RegleRattachement.estRacine(REGROUPEMENT)).isFalse();
        assertThat(RegleRattachement.parentsAutorises(PAYS)).isEmpty();
        assertThat(RegleRattachement.parentsAutorises(REGROUPEMENT))
                .containsExactlyInAnyOrder(PAYS, FSE, AGENCE, MASTER, MARCHAND, SOUS_DISTRIBUTEUR);
        assertThat(RegleRattachement.enfantsAutorises(REGROUPEMENT)).isEmpty();
    }

    @Test
    void niLesPaysNiLesFseNeSeDeplacent() {
        assertThat(RegleRattachement.estDeplacable(PAYS)).isFalse();
        assertThat(RegleRattachement.estDeplacable(FSE)).isFalse();
        assertThat(RegleRattachement.estDeplacable(AGENCE)).isTrue();
        assertThat(RegleRattachement.estDeplacable(SOUS_DISTRIBUTEUR)).isTrue();
        assertThat(RegleRattachement.estDeplacable(REGROUPEMENT)).isTrue();
    }

    @Test
    void enfantsAutorises() {
        assertThat(RegleRattachement.enfantsAutorises(PAYS)).containsExactly(FSE, REGROUPEMENT);
        assertThat(RegleRattachement.enfantsAutorises(FSE)).containsExactly(AGENCE, REGROUPEMENT);
        assertThat(RegleRattachement.enfantsAutorises(AGENCE)).containsExactly(MASTER, MARCHAND, REGROUPEMENT);
        assertThat(RegleRattachement.enfantsAutorises(MASTER)).containsExactly(SOUS_DISTRIBUTEUR, REGROUPEMENT);
        assertThat(RegleRattachement.enfantsAutorises(SOUS_DISTRIBUTEUR)).containsExactly(REGROUPEMENT);
        assertThat(RegleRattachement.enfantsAutorises(MARCHAND)).containsExactly(REGROUPEMENT);
    }

    @Test
    void descriptionLisible() {
        assertThat(RegleRattachement.description(SOUS_DISTRIBUTEUR)).isEqualTo("Un(e) Sous-distributeur se rattache à : Master.");
        assertThat(RegleRattachement.description(FSE)).isEqualTo("Un(e) FSE se rattache à : Pays.");
        assertThat(RegleRattachement.description(PAYS)).contains("racine");
        assertThat(RegleRattachement.description(REGROUPEMENT)).contains("n'importe quel niveau");
    }
}
