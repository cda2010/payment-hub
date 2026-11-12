package com.gateway.payment.catalog.capability;

/**
 * Le rôle vient de l'étape du parcours ({@code pay:actorRole} / binding catalogue).
 * DESTINATION ne vise le bénéficiaire que lorsque l'autre partie est une personne distincte.
 * Sinon (cash-in) le client de l'opération est déjà celui qui reçoit.
 */
final class RulesParty {

    private RulesParty() {
    }

    static boolean separateReceiver(
            String actorRole,
            String sourceCustomer,
            String destCustomer,
            String sourceAccount,
            String destAccount
    ) {
        if (!"DESTINATION".equalsIgnoreCase(actorRole)) {
            return false;
        }
        if (blank(destCustomer) && blank(destAccount)) {
            return false;
        }
        if (both(sourceCustomer, destCustomer) && !eq(sourceCustomer, destCustomer)) {
            return true;
        }
        return both(sourceAccount, destAccount) && !eq(sourceAccount, destAccount);
    }

    private static boolean both(String left, String right) {
        return !blank(left) && !blank(right);
    }

    private static boolean eq(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
