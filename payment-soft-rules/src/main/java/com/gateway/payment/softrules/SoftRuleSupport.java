package com.gateway.payment.softrules;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Helpers partagés par les handlers builtin. */
public final class SoftRuleSupport {

    private SoftRuleSupport() {}

    public static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    public static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String s = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s);
    }

    public static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Set<String> blockedSet(Object raw) {
        if (raw instanceof Collection<?> c) {
            return c.stream()
                    .map(v -> String.valueOf(v).trim().toUpperCase(Locale.ROOT))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> catalogConfiguration(Map<String, Object> context) {
        Object cfg = context.get("catalogConfiguration");
        if (cfg instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
