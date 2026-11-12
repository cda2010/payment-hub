package com.gateway.payment.catalog.gateway;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Section {@code gateway} de la configuration catalogue — start / providers sans Java produit.
 */
public final class CatalogGatewaySettings {

    private final Map<String, Object> raw;
    private final Map<String, Object> providersQuery;

    private CatalogGatewaySettings(Map<String, Object> raw, Map<String, Object> providersQuery) {
        this.raw = raw;
        this.providersQuery = providersQuery;
    }

    @SuppressWarnings("unchecked")
    public static CatalogGatewaySettings fromConfiguration(Map<String, Object> configuration) {
        if (configuration == null) {
            return new CatalogGatewaySettings(Map.of(), Map.of());
        }
        Object gateway = configuration.get("gateway");
        Map<String, Object> raw = gateway instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : Map.of();
        Object pq = raw.get("providersQuery");
        if (pq == null) {
            pq = raw.get("providers_query");
        }
        Map<String, Object> providersQuery = pq instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : Map.of();
        return new CatalogGatewaySettings(raw, providersQuery);
    }

    public String defaultCurrency(String fallback) {
        String v = str(raw.get("defaultCurrency"));
        if (v == null) {
            v = str(raw.get("default_currency"));
        }
        return v != null ? v : fallback;
    }

    public boolean optionalProviderAtStart() {
        Object v = raw.get("optionalProviderAtStart");
        if (v == null) {
            v = raw.get("optional_provider_at_start");
        }
        if (v == null) {
            return true;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }

    public boolean providerRequired() {
        Object v = raw.get("providerRequired");
        if (v == null) {
            v = raw.get("provider_required");
        }
        return v != null && Boolean.parseBoolean(String.valueOf(v));
    }

    public String pathAlias() {
        return first(raw, "pathAlias", "path_alias");
    }

    /** TRANSFER_EMITTERS | PROVIDERS (défaut). */
    public String providersSource() {
        String source = first(providersQuery, "source", "type");
        return source != null ? source.trim().toUpperCase(Locale.ROOT) : "PROVIDERS";
    }

    public FseParticipantKind participantKind() {
        String kind = first(providersQuery, "participantKind", "participant_kind", "kind");
        if (kind == null) {
            return null;
        }
        try {
            return FseParticipantKind.valueOf(kind.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public ServiceTypeCode serviceTypeFilter() {
        String st = first(providersQuery, "serviceType", "service_type");
        return ServiceTypeCode.fromString(st);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> hostFlow() {
        Object hf = raw.get("hostFlow");
        if (hf == null) {
            // aussi au top-level configuration
            return Map.of();
        }
        if (hf instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return Map.of();
    }

    public List<String> copyRequestFields() {
        Object rawFields = this.raw.get("copyRequestFields");
        if (rawFields == null) {
            rawFields = this.raw.get("copy_request_fields");
        }
        if (rawFields instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(StringUtils::hasText).toList();
        }
        return Collections.emptyList();
    }

    private static String first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            String v = str(map.get(key));
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }
}
