package com.gateway.payment.network.service;

import com.gateway.payment.network.domain.TypeUnite;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gateway.payment.network.domain.TypeUnite.AGENCE;
import static com.gateway.payment.network.domain.TypeUnite.FSE;
import static com.gateway.payment.network.domain.TypeUnite.MARCHAND;
import static com.gateway.payment.network.domain.TypeUnite.MASTER;
import static com.gateway.payment.network.domain.TypeUnite.PAYS;
import static com.gateway.payment.network.domain.TypeUnite.REGROUPEMENT;
import static com.gateway.payment.network.domain.TypeUnite.SOUS_DISTRIBUTEUR;

/**
 * Seul endroit qui dit quel type se rattache à quel autre. Le pays est la racine ; le regroupement se rattache à
 * n'importe quel niveau mais n'accueille aucune unité.
 */
public final class RegleRattachement {

    private static final Map<TypeUnite, Set<TypeUnite>> PARENTS = Map.of(
            FSE, EnumSet.of(PAYS),
            AGENCE, EnumSet.of(FSE),
            MASTER, EnumSet.of(AGENCE),
            MARCHAND, EnumSet.of(AGENCE),
            SOUS_DISTRIBUTEUR, EnumSet.of(MASTER),
            REGROUPEMENT, EnumSet.of(PAYS, FSE, AGENCE, MASTER, MARCHAND, SOUS_DISTRIBUTEUR)
    );

    private RegleRattachement() {
    }

    public static boolean estRacine(TypeUnite type) {
        return type == PAYS;
    }

    /** Un FSE reste dans son pays ; un pays n'a pas de parent. */
    public static boolean estDeplacable(TypeUnite type) {
        return type != PAYS && type != FSE;
    }

    public static Set<TypeUnite> parentsAutorises(TypeUnite type) {
        return PARENTS.getOrDefault(type, Set.of());
    }

    public static List<TypeUnite> enfantsAutorises(TypeUnite type) {
        return Arrays.stream(TypeUnite.values())
                .filter(enfant -> parentsAutorises(enfant).contains(type))
                .toList();
    }

    public static boolean peutRattacher(TypeUnite parent, TypeUnite enfant) {
        return parentsAutorises(enfant).contains(parent);
    }

    /** Message affichable : « Un(e) Agence se rattache à : FSE. » */
    public static String description(TypeUnite type) {
        if (type.estRegroupement()) {
            return "Un regroupement se rattache à une unité de n'importe quel niveau (pays, FSE, agence, master, "
                    + "marchand ou sous-distributeur) ; ses membres sont pris dans la branche de cette unité.";
        }
        if (estRacine(type)) {
            return "Un pays est à la racine du réseau : il n'a pas de parent.";
        }
        return "Un(e) " + type.libelle() + " se rattache à : "
                + String.join(", ", parentsAutorises(type).stream().map(TypeUnite::libelle).toList()) + ".";
    }
}
