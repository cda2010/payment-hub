package com.gateway.payment.catalog.journey;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Le montant est-il exigé dès la création du paiement, ou collecté en cours de
 * parcours ? La réponse ne vient que de la configuration catalogue : aucun code
 * produit n'est nommé ici, un nouveau produit se déclare donc sans Java.
 *
 * <p>Deux façons de l'exprimer :</p>
 * <ul>
 *   <li>{@code gateway.optionalAmountAtStart} — déclaration explicite ;</li>
 *   <li>{@code steps} — une étape collecte le montant ({@code CAPTURE_AMOUNT}),
 *       ou le parcours s'ouvre sur une étape qui le fait connaître
 *       ({@code LOOKUP}, {@code CAPTURE_REFERENCE}, {@code SELECT_PROVIDER}).</li>
 * </ul>
 *
 * <p>{@code CAPTURE_AMOUNT} compte où qu'elle soit dans le parcours : la placer en
 * troisième position ne rend pas le montant obligatoire à l'ouverture. Une étape
 * de découverte, elle, ne compte qu'en première position — plus loin, elle
 * n'empêche pas d'exiger un montant dès la création.</p>
 */
public final class CatalogAmountPolicy {

    /** Étape qui collecte le montant, quelle que soit sa position. */
    private static final String CAPTURE_AMOUNT = "CAPTURE_AMOUNT";

    /** Étapes d'ouverture au terme desquelles le montant devient connu. */
    private static final List<String> DISCOVERY_STEPS =
            List.of("LOOKUP", "CAPTURE_REFERENCE", "SELECT_PROVIDER");

    private CatalogAmountPolicy() {
    }

    public static boolean amountDeferredAtStart(Map<String, Object> configuration) {
        if (configuration == null) {
            return false;
        }
        if (declaredOptional(configuration)) {
            return true;
        }
        List<String> steps = steps(configuration);
        if (steps.isEmpty()) {
            return false;
        }
        if (steps.stream().anyMatch(step -> step.contains(CAPTURE_AMOUNT))) {
            return true;
        }
        String first = steps.get(0);
        return DISCOVERY_STEPS.stream().anyMatch(first::contains);
    }

    private static boolean declaredOptional(Map<String, Object> configuration) {
        if (!(configuration.get("gateway") instanceof Map<?, ?> gateway)) {
            return false;
        }
        Object flag = gateway.get("optionalAmountAtStart");
        if (flag == null) {
            flag = gateway.get("optional_amount_at_start");
        }
        return Boolean.TRUE.equals(flag) || "true".equalsIgnoreCase(String.valueOf(flag));
    }

    private static List<String> steps(Map<String, Object> configuration) {
        if (!(configuration.get("steps") instanceof List<?> raw)) {
            return List.of();
        }
        return raw.stream()
                .map(step -> String.valueOf(step).trim().toUpperCase(Locale.ROOT))
                .filter(step -> !step.isEmpty())
                .toList();
    }
}
