package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.fidelite.domain.Evenement;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Évalue une définition publiée. N'écrit rien. */
@Component
public class RuleEngine {

    private final ObjectMapper json = new ObjectMapper();

    public Outcome evaluate(String definition, Evenement event) {
        if (definition == null || definition.isBlank() || event == null) {
            return Outcome.skipped("Règle ou événement absent");
        }
        JsonNode root;
        try {
            root = json.readTree(definition);
        } catch (Exception e) {
            throw new IllegalArgumentException("Règle illisible");
        }
        String expected = text(root, "eventType");
        if (expected != null && event.eventType() != null && !expected.equals(event.eventType())) {
            return Outcome.skipped("Type d'événement différent");
        }
        JsonNode conditions = root.get("conditions");
        if (conditions != null && !conditions.isNull() && !matches(conditions, event)) {
            return Outcome.skipped("Conditions non remplies");
        }
        if (!covers(root.get("unite"), event)) {
            return Outcome.skipped("Unité destinataire différente");
        }
        long points = calculate(root.get("calculation"), event);
        JsonNode limits = root.get("limits");
        if (limits != null && limits.hasNonNull("maxPointsPerTransaction")) {
            points = Math.min(points, limits.get("maxPointsPerTransaction").asLong());
        }
        if (points <= 0) {
            return Outcome.skipped("Aucun point");
        }
        return new Outcome(true, points, null, root.path("exclusive").asBoolean(false));
    }

    /** L'unité de la règle est le destinataire, ou un de ses ancêtres, du même type. Absente, elle ne filtre pas. */
    private boolean covers(JsonNode unite, Evenement event) {
        if (unite == null || unite.isNull() || !unite.hasNonNull("id")) {
            return true;
        }
        String id = unite.get("id").asText().trim();
        if (id.isEmpty()) {
            return true;
        }
        String type = text(unite, "type");
        if (sameUnit(field("destinationUniteId", event), field("destinationUniteType", event), id, type)) {
            return true;
        }
        Object lineage = field("lignee", event);
        if (!(lineage instanceof List<?> nodes)) {
            return false;
        }
        for (Object node : nodes) {
            if (node instanceof Map<?, ?> row
                    && sameUnit(row.get("id"), row.get("type"), id, type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameUnit(Object actualId, Object actualType, String id, String type) {
        if (actualId == null || !id.equalsIgnoreCase(String.valueOf(actualId).trim())) {
            return false;
        }
        if (type == null) {
            return true;
        }
        return actualType != null && type.equalsIgnoreCase(String.valueOf(actualType).trim());
    }

    private long calculate(JsonNode calculation, Evenement event) {
        if (calculation == null || calculation.isNull()) {
            return 0;
        }
        String type = text(calculation, "type");
        if ("FIXED".equals(type)) {
            return calculation.path("points").asLong(0);
        }
        if (!"PROPORTIONAL".equals(type)) {
            return 0;
        }
        BigDecimal amount = event.amount() == null ? BigDecimal.ZERO : event.amount();
        BigDecimal unit = decimal(calculation, "unitAmount");
        BigDecimal perUnit = decimal(calculation, "pointsPerUnit");
        if (unit.signum() <= 0 || perUnit.signum() == 0 || amount.signum() <= 0) {
            return 0;
        }
        RoundingMode mode = rounding(text(calculation, "rounding"));
        return amount.divide(unit, 0, mode).multiply(perUnit).setScale(0, mode).longValue();
    }

    private boolean matches(JsonNode node, Evenement event) {
        if (node == null || node.isNull()) {
            return false;
        }
        String operator = text(node, "operator");
        if (operator == null) {
            return false;
        }
        return switch (operator.toUpperCase(Locale.ROOT)) {
            case "AND" -> every(node.get("rules"), event);
            case "OR" -> some(node.get("rules"), event);
            case "NOT" -> !matches(node.has("rule") ? node.get("rule") : first(node.get("rules")), event);
            case "EXISTS" -> field(text(node, "field"), event) != null;
            case "IN" -> contains(node.get("values"), field(text(node, "field"), event), true);
            case "NOT_IN" -> contains(node.get("values"), field(text(node, "field"), event), false);
            case "BETWEEN" -> between(node, field(text(node, "field"), event));
            case "EQ", "NEQ", "GT", "GTE", "LT", "LTE" -> compare(operator, field(text(node, "field"), event), node.get("value"));
            default -> false;
        };
    }

    private boolean every(JsonNode rules, Evenement event) {
        if (rules == null || !rules.isArray() || rules.isEmpty()) {
            return false;
        }
        for (JsonNode rule : rules) {
            if (!matches(rule, event)) {
                return false;
            }
        }
        return true;
    }

    private boolean some(JsonNode rules, Evenement event) {
        if (rules == null || !rules.isArray()) {
            return false;
        }
        for (JsonNode rule : rules) {
            if (matches(rule, event)) {
                return true;
            }
        }
        return false;
    }

    private static JsonNode first(JsonNode rules) {
        return rules != null && rules.isArray() && !rules.isEmpty() ? rules.get(0) : null;
    }

    private boolean contains(JsonNode values, Object actual, boolean inside) {
        if (values == null || !values.isArray() || actual == null) {
            return false;
        }
        for (JsonNode value : values) {
            if (compare("EQ", actual, value)) {
                return inside;
            }
        }
        return !inside;
    }

    private boolean between(JsonNode node, Object actual) {
        if (actual == null) {
            return false;
        }
        BigDecimal number = number(actual);
        if (number == null) {
            return false;
        }
        BigDecimal min = node.has("min") ? node.get("min").decimalValue() : null;
        BigDecimal max = node.has("max") ? node.get("max").decimalValue() : null;
        if ((min == null || max == null) && node.has("values") && node.get("values").isArray() && node.get("values").size() >= 2) {
            min = node.get("values").get(0).decimalValue();
            max = node.get("values").get(1).decimalValue();
        }
        if (min == null || max == null) {
            return false;
        }
        return number.compareTo(min) >= 0 && number.compareTo(max) <= 0;
    }

    private boolean compare(String operator, Object actual, JsonNode expected) {
        if (actual == null || expected == null || expected.isNull()) {
            return false;
        }
        BigDecimal left = number(actual);
        BigDecimal right = expected.isNumber() ? expected.decimalValue() : number(expected.asText());
        if (left != null && right != null) {
            int sign = left.compareTo(right);
            return switch (operator.toUpperCase(Locale.ROOT)) {
                case "EQ" -> sign == 0;
                case "NEQ" -> sign != 0;
                case "GT" -> sign > 0;
                case "GTE" -> sign >= 0;
                case "LT" -> sign < 0;
                case "LTE" -> sign <= 0;
                default -> false;
            };
        }
        boolean same = String.valueOf(actual).equals(expected.asText());
        return switch (operator.toUpperCase(Locale.ROOT)) {
            case "EQ" -> same;
            case "NEQ" -> !same;
            default -> false;
        };
    }

    private Object field(String name, Evenement event) {
        if (name == null) {
            return null;
        }
        return switch (name) {
            case "amount" -> event.amount();
            case "currency" -> event.currency();
            case "eventType" -> event.eventType();
            case "customerId" -> event.customerId();
            default -> event.attributes() == null ? null : event.attributes().get(name);
        };
    }

    private static BigDecimal decimal(JsonNode node, String name) {
        JsonNode value = node.get(name);
        if (value == null || value.isNull()) {
            return BigDecimal.ZERO;
        }
        return value.decimalValue();
    }

    private static BigDecimal number(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (value instanceof String text) {
            try {
                return new BigDecimal(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static RoundingMode rounding(String name) {
        if (name == null) {
            return RoundingMode.FLOOR;
        }
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "CEIL", "CEILING" -> RoundingMode.CEILING;
            case "HALF_UP" -> RoundingMode.HALF_UP;
            default -> RoundingMode.FLOOR;
        };
    }

    private static String text(JsonNode node, String name) {
        if (node == null || !node.hasNonNull(name)) {
            return null;
        }
        String value = node.get(name).asText();
        return value == null || value.isBlank() ? null : value;
    }

    public record Outcome(boolean applicable, long points, String motif, boolean exclusive) {
        public static Outcome skipped(String motif) {
            return new Outcome(false, 0, motif, false);
        }
    }
}
