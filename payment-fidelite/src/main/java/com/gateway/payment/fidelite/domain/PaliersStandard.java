package com.gateway.payment.fidelite.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Paliers posés sur un programme quand sa création n'en fournit pas.
 * Mêmes seuils et mêmes gains que l'écran fidélité du mobile.
 */
public final class PaliersStandard {

    public static final List<PalierSaisie> DEFAUT = List.of(
            new PalierSaisie("BRONZE", "Bronze", 0, new BigDecimal("1.00"),
                    List.of("1 pt / 1 000 F de paiement", "Alertes offres")),
            new PalierSaisie("SILVER", "Argent", 2_000, new BigDecimal("1.50"),
                    List.of("1,5 pt / 1 000 F", "Frais transfert réduits")),
            new PalierSaisie("GOLD", "Or", 8_000, new BigDecimal("2.00"),
                    List.of("2 pts / 1 000 F", "Cashback mensuel", "Support prioritaire")),
            new PalierSaisie("PLATINUM", "Platine", 20_000, new BigDecimal("3.00"),
                    List.of("3 pts / 1 000 F", "Invitations partenaires", "Conseiller dédié"))
    );

    private PaliersStandard() {
    }
}
