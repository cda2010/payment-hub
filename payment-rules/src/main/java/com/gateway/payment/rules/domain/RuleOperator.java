package com.gateway.payment.rules.domain;

public enum RuleOperator {
    GT, GTE, LT, LTE, EQ, NEQ, IN, BETWEEN;

    public static RuleOperator fromSymbol(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("opérateur requis");
        }
        return switch (raw.trim()) {
            case ">", "GT" -> GT;
            case ">=", "GTE" -> GTE;
            case "<", "LT" -> LT;
            case "<=", "LTE" -> LTE;
            case "=", "==", "EQ" -> EQ;
            case "!=", "<>", "NEQ" -> NEQ;
            case "IN" -> IN;
            case "BETWEEN" -> BETWEEN;
            default -> valueOf(raw.trim().toUpperCase());
        };
    }

    public String symbol() {
        return switch (this) {
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            case EQ -> "=";
            case NEQ -> "!=";
            case IN -> "IN";
            case BETWEEN -> "BETWEEN";
        };
    }
}
