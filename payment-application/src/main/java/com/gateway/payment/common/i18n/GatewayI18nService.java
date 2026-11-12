package com.gateway.payment.common.i18n;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GatewayI18nService {

    private final GatewayI18nProperties properties;

    public GatewayI18nService(GatewayI18nProperties properties) {
        this.properties = properties;
    }

    public GatewayI18nProperties properties() {
        return properties;
    }

    public List<GatewayI18nProperties.Language> supportedLanguages() {
        return properties.getSupportedLanguages();
    }

    public String currentLanguage() {
        String fromHeader = acceptLanguageFromRequest();
        if (fromHeader != null) {
            return properties.normalizeOrDefault(fromHeader);
        }
        return properties.normalizeOrDefault(properties.getDefaultLanguage());
    }

    private String acceptLanguageFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) {
            return null;
        }
        // ex. "en-US,en;q=0.9,fr;q=0.8" → en
        String first = header.split(",", 2)[0].trim();
        if (first.contains(";")) {
            first = first.split(";", 2)[0].trim();
        }
        return first.isBlank() ? null : first;
    }

    /** Ne conserve que les clés de langues déclarées dans la config. */
    public Map<String, String> filterSupportedMessages(Map<String, String> messages) {
        Map<String, String> normalized = LocalizedMessages.normalize(messages);
        if (normalized == null) {
            return null;
        }
        Map<String, String> filtered = new LinkedHashMap<>();
        for (String code : properties.supportedCodes()) {
            if (normalized.containsKey(code)) {
                filtered.put(code, normalized.get(code));
            }
        }
        return filtered.isEmpty() ? null : filtered;
    }

    public String resolveMessage(Map<String, String> messages) {
        return LocalizedMessages.resolve(messages, currentLanguage());
    }
}
