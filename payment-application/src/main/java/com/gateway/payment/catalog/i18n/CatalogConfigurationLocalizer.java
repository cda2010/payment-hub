package com.gateway.payment.catalog.i18n;

import com.gateway.payment.common.i18n.I18nScope;
import com.gateway.payment.common.i18n.TranslationService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Traduction des libellés portés par {@code catalog_de_service_version.configuration} :
 * {@code roles[].label}, {@code recap.*}, {@code presentation.sections[].title},
 * {@code history.*}, {@code notifications[].body/title}.
 * <p>
 * Les chemins produits ici sont ceux stockés dans {@code i18n_translation} (voir {@link I18nScope}).
 */
@Component
public class CatalogConfigurationLocalizer {

    private static final String[] RECAP_TEXT_KEYS = {"title", "subtitle", "confirmLabel"};
    private static final String[] HISTORY_TEXT_KEYS = {"kindLabel", "titleTemplate", "subtitleTemplate"};
    private static final String[] NOTIFICATION_TEXT_KEYS = {"title", "body"};

    private final TranslationService translations;

    public CatalogConfigurationLocalizer(TranslationService translations) {
        this.translations = translations;
    }

    /**
     * Libellés présents dans la configuration, indexés par chemin, dans la langue par défaut.
     * Permet d'amorcer les traductions dès la création du produit.
     */
    public Map<String, Map<String, String>> defaults(Map<String, Object> configuration) {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        String defaultLang = translations.defaultLanguage();
        forEachLabel(configuration, (path, text) -> out.put(path, new LinkedHashMap<>(Map.of(defaultLang, text))));
        return out;
    }

    /**
     * Fusionne {@link #defaults(Map)} et les traductions fournies par l'appelant :
     * la langue par défaut vient de la configuration, les autres langues de {@code provided}.
     */
    public Map<String, Map<String, String>> merge(
            Map<String, Object> configuration,
            Map<String, Map<String, String>> provided
    ) {
        Map<String, Map<String, String>> merged = defaults(configuration);
        if (provided != null) {
            provided.forEach((path, byLang) -> {
                if (path == null || path.isBlank() || byLang == null) {
                    return;
                }
                merged.computeIfAbsent(path.trim(), k -> new LinkedHashMap<>()).putAll(byLang);
            });
        }
        return merged;
    }

    /** Configuration dont les libellés sont résolus pour la langue de la requête courante. */
    public Map<String, Object> localize(UUID versionId, Map<String, Object> configuration) {
        if (versionId == null || configuration == null || configuration.isEmpty()) {
            return configuration;
        }
        return localize(translations.bundle(I18nScope.CATALOG_DE_SERVICE_VERSION, versionId), configuration);
    }

    public Map<String, Object> localize(TranslationService.Bundle bundle, Map<String, Object> configuration) {
        if (bundle == null || bundle.byPath().isEmpty() || configuration == null || configuration.isEmpty()) {
            return configuration;
        }
        Map<String, Object> copy = new LinkedHashMap<>(configuration);
        localizeRoles(copy, bundle);
        localizeRecap(copy, bundle);
        localizePresentation(copy, bundle);
        localizeHistory(copy, bundle);
        localizeNotifications(copy, bundle);
        return copy;
    }

    /** Section {@code history} déjà localisée (peut être null). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> historyOf(UUID versionId, Map<String, Object> configuration) {
        Map<String, Object> localized = localize(versionId, configuration);
        if (localized == null) {
            return null;
        }
        Object raw = localized.get("history");
        return raw instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : null;
    }

    /** Entrées {@code notifications} déjà localisées (liste vide si absent). */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> notificationsOf(UUID versionId, Map<String, Object> configuration) {
        Map<String, Object> localized = localize(versionId, configuration);
        if (localized == null) {
            return List.of();
        }
        Object raw = localized.get("notifications");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                out.add(new LinkedHashMap<>((Map<String, Object>) map));
            }
        }
        return out;
    }

    private void localizeRoles(Map<String, Object> configuration, TranslationService.Bundle bundle) {
        List<Object> roles = mutableList(configuration.get("roles"));
        if (roles == null) {
            return;
        }
        for (Object item : roles) {
            Map<String, Object> role = asMap(item);
            if (role == null) {
                continue;
            }
            String code = str(role.get("code"));
            String label = str(role.get("label"));
            if (code != null && label != null) {
                role.put("label", bundle.text(I18nScope.rolePath(code), label));
            }
        }
        configuration.put("roles", roles);
    }

    private void localizeRecap(Map<String, Object> configuration, TranslationService.Bundle bundle) {
        Map<String, Object> recap = mutableMap(configuration.get("recap"));
        if (recap == null) {
            return;
        }
        for (String key : RECAP_TEXT_KEYS) {
            String text = str(recap.get(key));
            if (text != null) {
                recap.put(key, bundle.text(I18nScope.recapPath(key), text));
            }
        }
        List<Object> fields = mutableList(recap.get("fields"));
        if (fields != null) {
            for (Object item : fields) {
                Map<String, Object> field = asMap(item);
                if (field == null) {
                    continue;
                }
                String key = str(field.get("key"));
                String label = str(field.get("label"));
                if (key != null && label != null) {
                    field.put("label", bundle.text(I18nScope.recapFieldPath(key), label));
                }
            }
            recap.put("fields", fields);
        }
        configuration.put("recap", recap);
    }

    private void localizePresentation(Map<String, Object> configuration, TranslationService.Bundle bundle) {
        Map<String, Object> presentation = mutableMap(configuration.get("presentation"));
        if (presentation == null) {
            return;
        }
        List<Object> sections = mutableList(presentation.get("sections"));
        if (sections == null) {
            return;
        }
        for (Object item : sections) {
            Map<String, Object> section = asMap(item);
            if (section == null) {
                continue;
            }
            String id = str(section.get("id"));
            String title = str(section.get("title"));
            if (id != null && title != null) {
                section.put("title", bundle.text(I18nScope.sectionTitlePath(id), title));
            }
        }
        presentation.put("sections", sections);
        configuration.put("presentation", presentation);
    }

    private void localizeHistory(Map<String, Object> configuration, TranslationService.Bundle bundle) {
        Map<String, Object> history = mutableMap(configuration.get("history"));
        if (history == null) {
            return;
        }
        for (String key : HISTORY_TEXT_KEYS) {
            String text = str(history.get(key));
            if (text != null) {
                history.put(key, bundle.text(I18nScope.historyPath(key), text));
            }
        }
        List<Object> fields = mutableList(history.get("detailFields"));
        if (fields != null) {
            for (Object item : fields) {
                Map<String, Object> field = asMap(item);
                if (field == null) {
                    continue;
                }
                String key = str(field.get("key"));
                String label = str(field.get("label"));
                if (key != null && label != null) {
                    field.put("label", bundle.text(I18nScope.historyDetailFieldPath(key), label));
                }
            }
            history.put("detailFields", fields);
        }
        configuration.put("history", history);
    }

    private void localizeNotifications(Map<String, Object> configuration, TranslationService.Bundle bundle) {
        List<Object> notifications = mutableList(configuration.get("notifications"));
        if (notifications == null) {
            return;
        }
        for (Object item : notifications) {
            Map<String, Object> notif = asMap(item);
            if (notif == null) {
                continue;
            }
            String code = str(notif.get("code"));
            if (code == null) {
                continue;
            }
            for (String field : NOTIFICATION_TEXT_KEYS) {
                String text = str(notif.get(field));
                if (text != null) {
                    notif.put(field, bundle.text(I18nScope.notificationPath(code, field), text));
                }
            }
        }
        configuration.put("notifications", notifications);
    }

    private void forEachLabel(Map<String, Object> configuration, LabelVisitor visitor) {
        if (configuration == null || configuration.isEmpty()) {
            return;
        }
        List<Object> roles = mutableList(configuration.get("roles"));
        if (roles != null) {
            for (Object item : roles) {
                Map<String, Object> role = asMap(item);
                String code = role == null ? null : str(role.get("code"));
                String label = role == null ? null : str(role.get("label"));
                if (code != null && label != null) {
                    visitor.accept(I18nScope.rolePath(code), label);
                }
            }
        }
        Map<String, Object> recap = mutableMap(configuration.get("recap"));
        if (recap != null) {
            for (String key : RECAP_TEXT_KEYS) {
                String text = str(recap.get(key));
                if (text != null) {
                    visitor.accept(I18nScope.recapPath(key), text);
                }
            }
            List<Object> fields = mutableList(recap.get("fields"));
            if (fields != null) {
                for (Object item : fields) {
                    Map<String, Object> field = asMap(item);
                    String key = field == null ? null : str(field.get("key"));
                    String label = field == null ? null : str(field.get("label"));
                    if (key != null && label != null) {
                        visitor.accept(I18nScope.recapFieldPath(key), label);
                    }
                }
            }
        }
        Map<String, Object> presentation = mutableMap(configuration.get("presentation"));
        if (presentation != null) {
            List<Object> sections = mutableList(presentation.get("sections"));
            if (sections != null) {
                for (Object item : sections) {
                    Map<String, Object> section = asMap(item);
                    String id = section == null ? null : str(section.get("id"));
                    String title = section == null ? null : str(section.get("title"));
                    if (id != null && title != null) {
                        visitor.accept(I18nScope.sectionTitlePath(id), title);
                    }
                }
            }
        }
        Map<String, Object> history = mutableMap(configuration.get("history"));
        if (history != null) {
            for (String key : HISTORY_TEXT_KEYS) {
                String text = str(history.get(key));
                if (text != null) {
                    visitor.accept(I18nScope.historyPath(key), text);
                }
            }
            List<Object> fields = mutableList(history.get("detailFields"));
            if (fields != null) {
                for (Object item : fields) {
                    Map<String, Object> field = asMap(item);
                    String key = field == null ? null : str(field.get("key"));
                    String label = field == null ? null : str(field.get("label"));
                    if (key != null && label != null) {
                        visitor.accept(I18nScope.historyDetailFieldPath(key), label);
                    }
                }
            }
        }
        List<Object> notifications = mutableList(configuration.get("notifications"));
        if (notifications != null) {
            for (Object item : notifications) {
                Map<String, Object> notif = asMap(item);
                String code = notif == null ? null : str(notif.get("code"));
                if (code == null) {
                    continue;
                }
                for (String field : NOTIFICATION_TEXT_KEYS) {
                    String text = str(notif.get(field));
                    if (text != null) {
                        visitor.accept(I18nScope.notificationPath(code, field), text);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mutableMap(Object raw) {
        return raw instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : null;
    }

    /**
     * Copie modifiable de la liste (la configuration d'origine peut être en cache),
     * en conservant les entrées non-objet telles quelles.
     */
    @SuppressWarnings("unchecked")
    private static List<Object> mutableList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return null;
        }
        List<Object> out = new ArrayList<>(list.size());
        for (Object item : list) {
            out.add(item instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : item);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object item) {
        return item instanceof Map<?, ?> ? (Map<String, Object>) item : null;
    }

    private static String str(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private interface LabelVisitor {
        void accept(String path, String text);
    }
}
