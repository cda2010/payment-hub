package com.gateway.payment.fidelite.domain;

import java.util.List;

/** Catalogue posé sur un programme à sa création. */
public final class RecompensesStandard {

    public static final List<RecompenseSaisie> DEFAUT = List.of(
            new RecompenseSaisie("RW-FEE", "Transfert sans frais", "1 transfert wallet gratuit", 500, "swap-horizontal-outline", "fee"),
            new RecompenseSaisie("RW-CASH-1K", "Cashback 1 000 F", "Crédité sur votre wallet", 1_000, "cash-outline", "cashback"),
            new RecompenseSaisie("RW-CASH-5K", "Cashback 5 000 F", "Crédité sur votre wallet", 4_500, "wallet-outline", "cashback"),
            new RecompenseSaisie("RW-AIRTIME", "Crédit téléphonique", "1 000 F de crédit mobile", 800, "phone-portrait-outline", "partner"),
            new RecompenseSaisie("RW-BILL", "Bon facture", "2 000 F sur une facture SONABEL / ONEA", 1_800, "flash-outline", "partner"),
            new RecompenseSaisie("RW-GIFT", "Carte cadeau partenaire", "Bon d’achat 10 000 F", 9_000, "gift-outline", "gift")
    );

    private RecompensesStandard() {
    }
}
