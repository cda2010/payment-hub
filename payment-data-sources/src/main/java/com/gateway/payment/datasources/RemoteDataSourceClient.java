package com.gateway.payment.datasources;

import com.gateway.payment.datasources.RemoteDataSourceProperties.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Appelle le Product Gateway d'une app peer (lecture seule).
 */
@Service
public class RemoteDataSourceClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteDataSourceClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RemoteDataSourceProperties properties;

    public RemoteDataSourceClient(RemoteDataSourceProperties properties) {
        this.properties = properties;
    }

    /** Codes d'apps configurées et actives (inventaire designer). */
    public List<AppDescriptor> listConfiguredApps() {
        List<AppDescriptor> out = new ArrayList<>();
        if (properties.getApps() == null) {
            return out;
        }
        for (Map.Entry<String, AppConfig> e : properties.getApps().entrySet()) {
            String code = normalize(e.getKey());
            AppConfig cfg = e.getValue();
            if (code == null || cfg == null || !cfg.isEnabled() || !StringUtils.hasText(cfg.getBaseUrl())) {
                continue;
            }
            String label = StringUtils.hasText(cfg.getLabel()) ? cfg.getLabel() : code;
            out.add(new AppDescriptor(code, label));
        }
        return List.copyOf(out);
    }

    public boolean isConfigured(String code) {
        AppConfig cfg = properties.find(code);
        return cfg != null && cfg.isEnabled() && StringUtils.hasText(cfg.getBaseUrl());
    }

    public RemoteDataSourceResult fetch(
            String code,
            Map<String, Object> params,
            String category,
            String q,
            int page,
            int size
    ) {
        AppConfig cfg = properties.find(code);
        if (cfg == null || !cfg.isEnabled()) {
            log.warn("GATEWAY data-source {} — app absente ou disabled", code);
            return RemoteDataSourceResult.empty();
        }
        if (!StringUtils.hasText(cfg.getBaseUrl())) {
            log.warn("GATEWAY data-source {} — base-url manquant", code);
            return RemoteDataSourceResult.empty();
        }

        String path = StringUtils.hasText(cfg.getListPath()) ? cfg.getListPath().trim() : "/api/v1/gateway/items";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        UriComponentsBuilder uri = UriComponentsBuilder
                .fromHttpUrl(trimSlash(cfg.getBaseUrl()) + path)
                .queryParam("page", Math.max(0, page))
                .queryParam("size", size > 0 ? size : 30);
        if (StringUtils.hasText(category)) {
            uri.queryParam("category", category.trim());
        }
        if (StringUtils.hasText(q)) {
            uri.queryParam("q", q.trim());
        }
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                String k = e.getKey().trim();
                if (k.isEmpty() || "codeFse".equalsIgnoreCase(k) || "providerCode".equalsIgnoreCase(k)) {
                    continue;
                }
                uri.queryParam(k, String.valueOf(e.getValue()));
            }
        }

        try {
            RestClient client = buildClient(cfg);
            Map<String, Object> body = client.get()
                    .uri(uri.build(true).toUri())
                    .retrieve()
                    .body(MAP_TYPE);
            return RemoteDataSourceBodyMapper.mapBody(body != null ? body : Map.of());
        } catch (RuntimeException ex) {
            log.warn("GATEWAY data-source {} — échec appel {}: {}", code, cfg.getBaseUrl(), ex.getMessage());
            return RemoteDataSourceResult.empty();
        }
    }

    private RestClient buildClient(AppConfig cfg) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(Math.max(200, cfg.getConnectTimeoutMs())))
                .withReadTimeout(Duration.ofMillis(Math.max(500, cfg.getReadTimeoutMs())));

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        String auth = cfg.getAuthType() != null
                ? cfg.getAuthType().trim().toLowerCase(Locale.ROOT)
                : "none";
        switch (auth) {
            case "bearer" -> {
                if (StringUtils.hasText(cfg.getBearerToken())) {
                    builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.getBearerToken().trim());
                }
            }
            case "api-key", "apikey" -> {
                String header = StringUtils.hasText(cfg.getApiKeyHeader()) ? cfg.getApiKeyHeader().trim() : "X-Api-Key";
                if (StringUtils.hasText(cfg.getApiKeyValue())) {
                    builder.defaultHeader(header, cfg.getApiKeyValue().trim());
                }
            }
            default -> { /* none */ }
        }
        return builder.build();
    }

    private static String trimSlash(String url) {
        String u = url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    public record AppDescriptor(String code, String label) {}
}
