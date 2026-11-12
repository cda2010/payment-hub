package com.gateway.payment.rules.moteur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.rules.domain.RuleOperator;

import java.math.BigDecimal;
import java.util.List;

final class OperatorEval {
    private OperatorEval() {}

    static boolean matches(
            RuleOperator op,
            Object actual,
            BigDecimal limitValue,
            String limitValuesJson,
            ObjectMapper mapper
    ) {
        if (op == null) {
            return false;
        }
        return switch (op) {
            case IN -> {
                List<String> allowed = parseList(limitValuesJson, mapper);
                yield actual != null && allowed.stream().anyMatch(a -> a.equalsIgnoreCase(String.valueOf(actual)));
            }
            case BETWEEN -> {
                List<String> bounds = parseList(limitValuesJson, mapper);
                if (bounds.size() < 2 || !(actual instanceof BigDecimal bd)) {
                    yield false;
                }
                BigDecimal lo = new BigDecimal(bounds.get(0));
                BigDecimal hi = new BigDecimal(bounds.get(1));
                yield bd.compareTo(lo) >= 0 && bd.compareTo(hi) <= 0;
            }
            case EQ -> {
                if (limitValuesJson != null && !limitValuesJson.isBlank()) {
                    yield actual != null && parseList(limitValuesJson, mapper).stream()
                            .anyMatch(a -> a.equalsIgnoreCase(String.valueOf(actual)));
                }
                if (limitValue != null) {
                    yield compareNumeric(actual, limitValue) == 0;
                }
                yield actual == null;
            }
            case NEQ -> !matches(RuleOperator.EQ, actual, limitValue, limitValuesJson, mapper);
            case GT -> compareNumeric(actual, limitValue) > 0;
            case GTE -> compareNumeric(actual, limitValue) >= 0;
            case LT -> compareNumeric(actual, limitValue) < 0;
            case LTE -> compareNumeric(actual, limitValue) <= 0;
        };
    }

    /** La règle est respectée si la comparaison « actual op limit » est vraie. */
    static boolean satisfied(
            RuleOperator op,
            Object actual,
            BigDecimal limitValue,
            String limitValuesJson,
            ObjectMapper mapper
    ) {
        return matches(op, actual, limitValue, limitValuesJson, mapper);
    }

    private static int compareNumeric(Object actual, BigDecimal limitValue) {
        BigDecimal a = toDecimal(actual);
        BigDecimal b = limitValue == null ? BigDecimal.ZERO : limitValue;
        return a.compareTo(b);
    }

    private static BigDecimal toDecimal(Object actual) {
        if (actual == null) {
            return BigDecimal.ZERO;
        }
        if (actual instanceof BigDecimal bd) {
            return bd;
        }
        if (actual instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(actual));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static List<String> parseList(String json, ObjectMapper mapper) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of(json.split(","));
        }
    }
}
