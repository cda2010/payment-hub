package com.gateway.payment.datasources;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gateways d'apps peers — {@code gateway.data-sources.apps.<CODE>}.
 */
@ConfigurationProperties(prefix = "gateway.data-sources")
public class RemoteDataSourceProperties {

    private Map<String, AppConfig> apps = new LinkedHashMap<>();

    public Map<String, AppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, AppConfig> apps) {
        this.apps = apps != null ? apps : new LinkedHashMap<>();
    }

    public AppConfig find(String code) {
        if (code == null || code.isBlank() || apps == null || apps.isEmpty()) {
            return null;
        }
        String key = code.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        AppConfig direct = apps.get(key);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<String, AppConfig> e : apps.entrySet()) {
            if (e.getKey() != null && e.getKey().trim().toUpperCase(Locale.ROOT).replace('-', '_').equals(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    public static class AppConfig {
        /** Active l'app (défaut true si base-url présent). */
        private boolean enabled = true;
        private String label;
        /** URL de base du Product Gateway peer (sans slash final). */
        private String baseUrl;
        /** Chemin liste, ex. {@code /api/v1/gateway/establishments}. */
        private String listPath = "/api/v1/gateway/items";
        /** {@code none} | {@code bearer} | {@code api-key} */
        private String authType = "none";
        private String bearerToken;
        private String apiKeyHeader = "X-Api-Key";
        private String apiKeyValue;
        private int connectTimeoutMs = 2_000;
        private int readTimeoutMs = 5_000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getListPath() {
            return listPath;
        }

        public void setListPath(String listPath) {
            this.listPath = listPath;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getBearerToken() {
            return bearerToken;
        }

        public void setBearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKeyValue() {
            return apiKeyValue;
        }

        public void setApiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}
