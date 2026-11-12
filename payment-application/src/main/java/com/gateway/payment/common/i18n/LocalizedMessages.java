package com.gateway.payment.common.i18n;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Résolution de libellés multi-langue ({@code fr}, {@code en}, …).
 */
public final class LocalizedMessages {

    public static final String FR = "fr";
    public static final String EN = "en";

    private LocalizedMessages() {}

    public static Map<String, String> of(String fr, String en) {
        Map<String, String> map = new LinkedHashMap<>();
        if (fr != null && !fr.isBlank()) {
            map.put(FR, fr.trim());
        }
        if (en != null && !en.isBlank()) {
            map.put(EN, en.trim());
        }
        return map.isEmpty() ? null : map;
    }

    /** Normalise une map (clés en minuscules, valeurs trim). */
    public static Map<String, String> normalize(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        Map<String, String> out = new LinkedHashMap<>();
        raw.forEach((k, v) -> {
            if (k == null || k.isBlank() || v == null || v.isBlank()) {
                return;
            }
            out.put(k.trim().toLowerCase(Locale.ROOT), v.trim());
        });
        return out.isEmpty() ? null : out;
    }

    /**
     * Choisit le texte pour {@code lang} (ex. {@code fr}, {@code en-US}).
     * Fallback : fr → en → première valeur.
     */
    public static String resolve(Map<String, String> messages, String lang) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        if (lang != null && !lang.isBlank()) {
            String normalized = lang.trim().toLowerCase(Locale.ROOT).replace('_', '-');
            String exact = messages.get(normalized);
            if (exact != null) {
                return exact;
            }
            String base = normalized.split("-", 2)[0];
            String byBase = messages.get(base);
            if (byBase != null) {
                return byBase;
            }
        }
        if (messages.containsKey(FR)) {
            return messages.get(FR);
        }
        if (messages.containsKey(EN)) {
            return messages.get(EN);
        }
        return messages.values().iterator().next();
    }

    /** Affichage admin compact : {@code fr / en}. */
    public static String preview(Map<String, String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        String fr = messages.get(FR);
        String en = messages.get(EN);
        if (fr != null && en != null && !fr.equals(en)) {
            return fr + " / " + en;
        }
        return resolve(messages, FR);
    }
}
