package com.gateway.payment.common.i18n;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ConfigurationProperties(prefix = "gateway.i18n")
public class GatewayI18nProperties {

    private String defaultLanguage = "fr";
    private List<Language> supportedLanguages = new ArrayList<>(List.of(
            new Language("fr", "Français"),
            new Language("en", "English")
    ));

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public List<Language> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<Language> supportedLanguages) {
        this.supportedLanguages = supportedLanguages != null ? supportedLanguages : List.of();
    }

    public List<String> supportedCodes() {
        return supportedLanguages.stream()
                .map(Language::getCode)
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    public boolean isSupported(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String base = code.trim().toLowerCase(Locale.ROOT).replace('_', '-').split("-", 2)[0];
        return supportedCodes().contains(base);
    }

    public String normalizeOrDefault(String lang) {
        if (lang != null && !lang.isBlank()) {
            String base = lang.trim().toLowerCase(Locale.ROOT).replace('_', '-').split("-", 2)[0];
            if (supportedCodes().contains(base)) {
                return base;
            }
        }
        String def = defaultLanguage != null ? defaultLanguage.trim().toLowerCase(Locale.ROOT) : "fr";
        return supportedCodes().contains(def) ? def : supportedCodes().stream().findFirst().orElse("fr");
    }

    public static class Language {
        private String code;
        private String label;

        public Language() {}

        public Language(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
