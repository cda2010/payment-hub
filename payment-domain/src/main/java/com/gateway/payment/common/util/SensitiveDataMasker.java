package com.gateway.payment.common.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "otp", "password", "secret", "clientsecret", "apikey", "authorization", "token", "pin"
    );
    private static final Pattern OTP_PATTERN = Pattern.compile(
            "\"(otp|pin|password|secret|token|apiKey|clientSecret)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE
    );

    private SensitiveDataMasker() {}

    public static String maskJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload;
        }
        return OTP_PATTERN.matcher(payload).replaceAll("\"$1\":\"***\"");
    }

    public static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        return SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", ""));
    }

    public static String maskValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
