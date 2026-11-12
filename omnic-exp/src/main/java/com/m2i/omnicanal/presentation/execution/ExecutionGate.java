package com.m2i.omnicanal.presentation.execution;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Décide si un ordre part chez le partenaire, attend un OTP, ou rejoue le résultat déjà connu.
 */
public final class ExecutionGate {

    public static final int OTP_REQUIS = 10;
    public static final int ACCEPTE = 20;

    public enum Action {
        CALL, REPLAY, HOLD_OTP
    }

    public record Decision(Action action) {
    }

    private ExecutionGate() {
    }

    public static boolean readOnly(String httpMethod, String codeMethode) {
        if (httpMethod != null && "GET".equalsIgnoreCase(httpMethod.trim())) {
            return true;
        }
        if (codeMethode == null) {
            return false;
        }
        String code = codeMethode.toUpperCase(Locale.ROOT);
        return code.contains("CONSULT")
                || code.contains("GETINFO")
                || code.contains("SOLDE")
                || code.contains("FRAIS")
                || code.contains("STATUS")
                || code.contains("HISTO")
                || code.contains("CHECK");
    }

    public static boolean otpDialogue(String codeMethode) {
        if (codeMethode == null) {
            return false;
        }
        return codeMethode.toUpperCase(Locale.ROOT).contains("OTP");
    }

    public static String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "DIRECT";
        }
        String value = mode.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "OTP", "ASYNC", "DIRECT" -> value;
            default -> "DIRECT";
        };
    }

    public static boolean hasOtp(Map<String, String> raw) {
        if (raw == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (isOtpKey(entry.getKey()) && entry.getValue() != null && !entry.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    public static String explicitKey(Map<String, String> raw) {
        if (raw == null) {
            return null;
        }
        for (String name : new String[] {"idempotencyKey", "IDEMPOTENCY_KEY", "Idempotency-Key", "paymentId", "reference", "REFERENCE", "transactionReference"}) {
            String value = raw.get(name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("idempotencyKey")
                    && entry.getValue() != null && !entry.getValue().isBlank()) {
                return entry.getValue().trim();
            }
        }
        return null;
    }

    public static String fingerprint(String codeFse, String codeMethode, Map<String, String> raw) {
        TreeMap<String, String> sorted = new TreeMap<>();
        if (raw != null) {
            raw.forEach((key, value) -> {
                if (key == null || value == null || value.isBlank() || isOtpKey(key) || isKeyName(key)) {
                    return;
                }
                sorted.put(key, value.trim());
            });
        }
        StringBuilder builder = new StringBuilder();
        builder.append(codeFse == null ? "" : codeFse.trim()).append('|');
        builder.append(codeMethode == null ? "" : codeMethode.trim()).append('|');
        sorted.forEach((key, value) -> builder.append(key).append('=').append(value).append('&'));
        return sha256(builder.toString());
    }

    public static Decision decide(String mode, boolean otpDialogue, boolean hasOtp, String statut) {
        if (statut != null) {
            if ("EN_ATTENTE_OTP".equals(statut) && hasOtp) {
                return new Decision(Action.CALL);
            }
            if ("EN_ATTENTE_OTP".equals(statut)) {
                return new Decision(Action.HOLD_OTP);
            }
            return new Decision(Action.REPLAY);
        }
        if ("OTP".equals(mode) && !otpDialogue && !hasOtp) {
            return new Decision(Action.HOLD_OTP);
        }
        return new Decision(Action.CALL);
    }

    public static boolean infrastructure(int errCode) {
        return errCode == -100 || errCode == -200 || errCode == -400;
    }

    private static boolean isOtpKey(String key) {
        if (key == null) {
            return false;
        }
        String value = key.toUpperCase(Locale.ROOT);
        return "OTP".equals(value) || "CODEOTP".equals(value) || "CODE_OTP".equals(value);
    }

    private static boolean isKeyName(String key) {
        String value = key.toUpperCase(Locale.ROOT);
        return value.contains("IDEMPOTENCY") || "PAYMENTID".equals(value) || "REFERENCE".equals(value)
                || "TRANSACTIONREFERENCE".equals(value);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
