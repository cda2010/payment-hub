package com.gateway.payment.identity.enrolement;

/**
 * Parcours ENROLEMENT. Une étape n'est ouverte que par une transition prévue.
 * Après le code, l'identité est recueillie, puis la question du compte.
 * L'adresse précède les pièces seulement quand la personne s'identifie.
 */
public final class EnrolementWorkflow {

    public static final String NOM = "ENROLEMENT";

    private EnrolementWorkflow() {
    }

    public static void vers(String actuel, String cible) {
        if (!autorise(actuel, cible)) {
            throw new IllegalArgumentException("Le workflow n'ouvre pas cette étape");
        }
    }

    static boolean autorise(String actuel, String cible) {
        if (actuel == null || cible == null) {
            return false;
        }
        return switch (actuel) {
            case "CODE" -> "ENTRE".equals(cible) || "PROPOSER".equals(cible) || "DOSSIER".equals(cible);
            case "DOSSIER" -> "PROPOSER".equals(cible);
            case "PROPOSER" -> "SUITE".equals(cible) || "TERMINE".equals(cible);
            case "SUITE" -> "PRET".equals(cible) || "ADRESSE".equals(cible);
            case "ADRESSE" -> "PIECE".equals(cible);
            case "PRET", "PIECE" -> "OUVERT".equals(cible);
            default -> false;
        };
    }
}
