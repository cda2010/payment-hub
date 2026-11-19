package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.service.RuleEngine.Outcome;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/** Compte une fenêtre d'événements. N'écrit rien. */
public final class FenetreCalcul {

    private static final ObjectMapper JSON = new ObjectMapper();

    private FenetreCalcul() {
    }

    public static Outcome evaluer(String definition, Evenement event, List<Evenement> histoire) {
        if (definition == null || event == null) {
            return Outcome.skipped("Règle ou événement absent");
        }
        JsonNode root;
        try {
            root = JSON.readTree(definition);
        } catch (Exception e) {
            return Outcome.skipped("Règle illisible");
        }
        JsonNode calculation = root.get("calculation");
        if (calculation == null || !"WINDOW".equals(text(calculation, "type"))) {
            return Outcome.skipped("Pas une fenêtre");
        }
        int days = calculation.path("windowDays").asInt(0);
        BigDecimal threshold = calculation.path("threshold").decimalValue();
        long points = calculation.path("points").asLong(0);
        if (threshold.signum() <= 0 || points <= 0) {
            return Outcome.skipped("Fenêtre incomplète");
        }
        String metric = text(calculation, "metric");
        if (metric == null) {
            metric = "COUNT";
        }
        Instant fin = Instant.now();
        Instant debut = days <= 0 ? Instant.EPOCH : fin.minusSeconds(days * 86_400L);
        BigDecimal compte = BigDecimal.ZERO;
        java.util.Set<String> distincts = new java.util.HashSet<>();
        if (histoire != null) {
            for (Evenement passe : histoire) {
                if (passe == null || passe.eventId() == null || !dansFenetre(passe, debut)) {
                    continue;
                }
                if (!correspond(calculation, passe)) {
                    continue;
                }
                switch (metric.toUpperCase(Locale.ROOT)) {
                    case "AMOUNT" -> compte = compte.add(passe.amount() == null ? BigDecimal.ZERO : passe.amount());
                    case "DISTINCT" -> {
                        String service = attribut(passe, "serviceType");
                        if (service != null) {
                            distincts.add(service.toUpperCase(Locale.ROOT));
                        }
                    }
                    default -> compte = compte.add(BigDecimal.ONE);
                }
            }
        }
        if ("DISTINCT".equalsIgnoreCase(metric)) {
            compte = BigDecimal.valueOf(distincts.size());
        }
        if (compte.compareTo(threshold) < 0) {
            return Outcome.skipped("Fenêtre non atteinte");
        }
        LocalDate ancre = days <= 0
                ? LocalDate.of(1970, 1, 1)
                : LocalDate.ofInstant(fin, ZoneOffset.UTC);
        String reference = "WINDOW:" + ancre + ":" + metric + ":" + days;
        return new Outcome(true, points, "REF:" + reference, root.path("exclusive").asBoolean(false));
    }

    private static boolean dansFenetre(Evenement event, Instant debut) {
        String raw = attribut(event, "receivedAt");
        if (raw == null) {
            return true;
        }
        try {
            return !Instant.parse(raw).isBefore(debut);
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean correspond(JsonNode calculation, Evenement event) {
        return egal(text(calculation, "productCode"), attribut(event, "productCode"))
                && egal(text(calculation, "serviceType"), attribut(event, "serviceType"))
                && egal(text(calculation, "channel"), attribut(event, "channel"));
    }

    private static boolean egal(String attendu, String reel) {
        return attendu == null || attendu.equalsIgnoreCase(reel == null ? "" : reel);
    }

    private static String attribut(Evenement event, String key) {
        if (event.attributes() == null || event.attributes().get(key) == null) {
            return null;
        }
        String value = event.attributes().get(key).toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String text(JsonNode node, String name) {
        if (node == null || !node.hasNonNull(name)) {
            return null;
        }
        String value = node.get(name).asText();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
