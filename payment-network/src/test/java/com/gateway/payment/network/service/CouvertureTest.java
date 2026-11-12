package com.gateway.payment.network.service;

import com.gateway.payment.network.Reseau;
import com.gateway.payment.network.domain.UniteOrganisation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gateway.payment.network.domain.TypeUnite.AGENCE;
import static com.gateway.payment.network.domain.TypeUnite.FSE;
import static com.gateway.payment.network.domain.TypeUnite.MASTER;
import static org.assertj.core.api.Assertions.assertThat;

class CouvertureTest {

    private final UniteOrganisation fse = Reseau.sous(Reseau.pays("BF", "UEMOA"), FSE, "FSE");
    private final UniteOrganisation agA = Reseau.sous(fse, AGENCE, "AG-A");
    private final UniteOrganisation agB = Reseau.sous(fse, AGENCE, "AG-B");
    private final UniteOrganisation masterA = Reseau.sous(agA, MASTER, "MS-A");

    @Test
    void membresDeBranchesDistinctesNeSeChevauchentPas() {
        assertThat(Couverture.premierChevauchement(List.of(position(agB), position(masterA)))).isEmpty();
    }

    @Test
    void unMembreSousUnAutreMembreEstUnDoublon() {
        var chevauchement = Couverture.premierChevauchement(List.of(position(masterA), position(agA))).orElseThrow();

        assertThat(chevauchement.ascendant()).isSameAs(agA);
        assertThat(chevauchement.descendant()).isSameAs(masterA);
    }

    @Test
    void unPrefixeDeTexteNEstPasUneDescendance() {
        UniteOrganisation x = Reseau.sous(fse, AGENCE, "X");
        x.setChemin(fse.getChemin() + "ab/");
        UniteOrganisation y = Reseau.sous(fse, AGENCE, "Y");
        y.setChemin(fse.getChemin() + "abc/");

        assertThat(Couverture.premierChevauchement(List.of(position(x), position(y)))).isEmpty();
    }

    private static Couverture.Position position(UniteOrganisation u) {
        return new Couverture.Position(u, u.getChemin());
    }
}
