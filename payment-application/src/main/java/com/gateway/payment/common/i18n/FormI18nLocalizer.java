package com.gateway.payment.common.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internationalise un schéma Form.io à partir d'un bloc {@code i18n}
 * (racine du JSON formulaire ou sous {@code formio.i18n}).
 * <pre>
 * "i18n": {
 *   "en": { "Numéro de téléphone": "Phone number" },
 *   "fr": { "Numéro de téléphone": "Numéro de téléphone" }
 * }
 * </pre>
 * Les labels des composants (et placeholders) sont remplacés pour la langue demandée.
 */
@Component
public class FormI18nLocalizer {

    private final ObjectMapper objectMapper;
    private final GatewayI18nService i18nService;

    public FormI18nLocalizer(ObjectMapper objectMapper, GatewayI18nService i18nService) {
        this.objectMapper = objectMapper;
        this.i18nService = i18nService;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> localizeFormBody(Map<String, Object> body, String lang) {
        if (body == null) {
            return null;
        }
        String language = i18nService.properties().normalizeOrDefault(lang);
        Map<String, Object> copy = objectMapper.convertValue(body, new TypeReference<>() {});
        Map<String, String> dictionary = resolveDictionary(copy, language);

        Object formio = copy.get("formio");
        if (formio instanceof Map<?, ?> formioMap) {
            Map<String, Object> formioCopy = objectMapper.convertValue(formioMap, new TypeReference<>() {});
            applyDictionary(formioCopy, dictionary);
            // Form.io client peut aussi consommer i18n natif
            Map<String, Object> formioI18n = new LinkedHashMap<>();
            Object rawI18n = copy.get("i18n");
            if (rawI18n instanceof Map<?, ?> rootI18n) {
                rootI18n.forEach((k, v) -> {
                    if (k != null && v instanceof Map<?, ?>) {
                        formioI18n.put(String.valueOf(k), v);
                    }
                });
            }
            if (!formioI18n.isEmpty()) {
                formioCopy.put("i18n", formioI18n);
            }
            copy.put("formio", formioCopy);
        }
        return copy;
    }

    public Map<String, Object> localizeFormioSchema(Map<String, Object> formioSchema, String lang) {
        if (formioSchema == null) {
            return null;
        }
        String language = i18nService.properties().normalizeOrDefault(lang);
        Map<String, Object> copy = objectMapper.convertValue(formioSchema, new TypeReference<>() {});
        Map<String, String> dictionary = dictionaryFromFormio(copy, language);
        applyDictionary(copy, dictionary);
        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> resolveDictionary(Map<String, Object> body, String language) {
        Map<String, String> dict = new LinkedHashMap<>();
        Object root = body.get("i18n");
        if (root instanceof Map<?, ?> rootMap) {
            Object langMap = rootMap.get(language);
            if (langMap instanceof Map<?, ?> translations) {
                translations.forEach((k, v) -> {
                    if (k != null && v != null) {
                        dict.put(String.valueOf(k), String.valueOf(v));
                    }
                });
            }
        }
        Object formio = body.get("formio");
        if (formio instanceof Map<?, ?> fm) {
            dict.putAll(dictionaryFromFormio((Map<String, Object>) fm, language));
        }
        return dict;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> dictionaryFromFormio(Map<String, Object> formio, String language) {
        Map<String, String> dict = new LinkedHashMap<>();
        Object i18n = formio.get("i18n");
        if (!(i18n instanceof Map<?, ?> i18nMap)) {
            return dict;
        }
        Object langMap = i18nMap.get(language);
        if (langMap instanceof Map<?, ?> translations) {
            translations.forEach((k, v) -> {
                if (k != null && v != null) {
                    dict.put(String.valueOf(k), String.valueOf(v));
                }
            });
        }
        return dict;
    }

    @SuppressWarnings("unchecked")
    private void applyDictionary(Map<String, Object> node, Map<String, String> dictionary) {
        if (dictionary == null || dictionary.isEmpty()) {
            return;
        }
        for (String field : List.of("label", "placeholder", "description", "tooltip", "content", "title")) {
            Object value = node.get(field);
            if (value instanceof String s && dictionary.containsKey(s)) {
                node.put(field, dictionary.get(s));
            }
        }
        Object components = node.get("components");
        if (components instanceof List<?> list) {
            List<Object> next = new ArrayList<>();
            for (Object c : list) {
                if (c instanceof Map<?, ?> m) {
                    Map<String, Object> child = objectMapper.convertValue(m, new TypeReference<>() {});
                    applyDictionary(child, dictionary);
                    next.add(child);
                } else {
                    next.add(c);
                }
            }
            node.put("components", next);
        }
        Object columns = node.get("columns");
        if (columns instanceof List<?> cols) {
            List<Object> next = new ArrayList<>();
            for (Object c : cols) {
                if (c instanceof Map<?, ?> m) {
                    Map<String, Object> child = objectMapper.convertValue(m, new TypeReference<>() {});
                    applyDictionary(child, dictionary);
                    next.add(child);
                } else {
                    next.add(c);
                }
            }
            node.put("columns", next);
        }
    }
}
