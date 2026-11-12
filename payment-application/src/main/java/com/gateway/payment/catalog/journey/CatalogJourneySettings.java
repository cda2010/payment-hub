package com.gateway.payment.catalog.journey;

import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.common.domain.ProductAudience;
import com.gateway.payment.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Journey + Channel + Audience sur la configuration catalogue.
 * <pre>
 * PRODUCT (CASH_OUT)
 *   └── journeyCode: STANDARD_CASH_OUT
 *         ├── supportedChannels / channelDefaults
 *         └── supportedAudiences / defaultAudience
 * </pre>
 *
 * <p>{@link AccessChannel} = surface d'accès (MOBILE, WEB…).
 * {@link ProductAudience} = segment client (RETAIL, CORPORATE…).
 */
public final class CatalogJourneySettings {

    private final Map<String, Object> configuration;
    private final String journeyCode;
    private final Set<AccessChannel> supportedChannels;
    private final AccessChannel defaultChannel;
    private final Map<String, Map<String, Object>> channelDefaults;
    private final Set<ProductAudience> supportedAudiences;
    private final ProductAudience defaultAudience;

    private CatalogJourneySettings(
            Map<String, Object> configuration,
            String journeyCode,
            Set<AccessChannel> supportedChannels,
            AccessChannel defaultChannel,
            Map<String, Map<String, Object>> channelDefaults,
            Set<ProductAudience> supportedAudiences,
            ProductAudience defaultAudience
    ) {
        this.configuration = configuration;
        this.journeyCode = journeyCode;
        this.supportedChannels = supportedChannels;
        this.defaultChannel = defaultChannel;
        this.channelDefaults = channelDefaults;
        this.supportedAudiences = supportedAudiences;
        this.defaultAudience = defaultAudience;
    }

    @SuppressWarnings("unchecked")
    public static CatalogJourneySettings from(Map<String, Object> configuration) {
        Map<String, Object> cfg = configuration != null
                ? new LinkedHashMap<>(configuration)
                : new LinkedHashMap<>();

        String journey = str(cfg.get("journeyCode"));
        if (journey == null) {
            journey = str(cfg.get("journey_code"));
        }

        Set<AccessChannel> supported = new LinkedHashSet<>();
        Object rawSupported = cfg.get("supportedChannels");
        if (rawSupported == null) {
            rawSupported = cfg.get("supported_channels");
        }
        if (rawSupported instanceof List<?> list) {
            for (Object item : list) {
                AccessChannel ch = AccessChannel.parse(String.valueOf(item));
                if (ch != null) {
                    supported.add(ch);
                }
            }
        }

        AccessChannel defaultChannel = AccessChannel.parse(first(cfg, "defaultChannel", "default_channel"));
        if (defaultChannel == null && !supported.isEmpty()) {
            defaultChannel = supported.iterator().next();
        }

        Map<String, Map<String, Object>> overlays = new LinkedHashMap<>();
        Object rawDefaults = cfg.get("channelDefaults");
        if (rawDefaults == null) {
            rawDefaults = cfg.get("channel_defaults");
        }
        if (rawDefaults instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() == null || !(e.getValue() instanceof Map<?, ?>)) {
                    continue;
                }
                String key = String.valueOf(e.getKey()).trim().toUpperCase(Locale.ROOT);
                overlays.put(key, new LinkedHashMap<>((Map<String, Object>) e.getValue()));
            }
        }

        Set<ProductAudience> audiences = new LinkedHashSet<>();
        Object rawAudiences = cfg.get("supportedAudiences");
        if (rawAudiences == null) {
            rawAudiences = cfg.get("supported_audiences");
        }
        if (rawAudiences instanceof List<?> list) {
            for (Object item : list) {
                ProductAudience a = ProductAudience.parse(String.valueOf(item));
                if (a != null) {
                    audiences.add(a);
                }
            }
        }

        ProductAudience defaultAudience = ProductAudience.parse(
                first(cfg, "defaultAudience", "default_audience"));
        if (defaultAudience == null && !audiences.isEmpty()) {
            defaultAudience = audiences.iterator().next();
        }

        return new CatalogJourneySettings(
                cfg,
                journey,
                Set.copyOf(supported),
                defaultChannel,
                Map.copyOf(overlays),
                Set.copyOf(audiences),
                defaultAudience
        );
    }

    public String journeyCode() {
        return journeyCode;
    }

    public String resolveJourneyCode(String requested) {
        if (StringUtils.hasText(requested)) {
            return requested.trim().toUpperCase(Locale.ROOT);
        }
        return journeyCode;
    }

    public Set<AccessChannel> supportedChannels() {
        return supportedChannels;
    }

    public boolean hasChannelPolicy() {
        return !supportedChannels.isEmpty();
    }

    public AccessChannel defaultChannel() {
        return defaultChannel;
    }

    public Set<ProductAudience> supportedAudiences() {
        return supportedAudiences;
    }

    public boolean hasAudiencePolicy() {
        return !supportedAudiences.isEmpty();
    }

    public ProductAudience defaultAudience() {
        return defaultAudience;
    }

    /**
     * {@code true} si le produit est visible pour l'audience demandée.
     * Sans politique d'audience déclarée → visible pour toutes les audiences.
     */
    public boolean supportsAudience(ProductAudience audience) {
        if (audience == null) {
            return true;
        }
        if (!hasAudiencePolicy()) {
            return true;
        }
        return supportedAudiences.contains(audience);
    }

    public boolean supportsAudience(String raw) {
        return supportsAudience(ProductAudience.parse(raw));
    }

    /**
     * Résout le canal (request → défaut) et valide {@code supportedChannels} si déclarés.
     */
    public AccessChannel resolveChannel(String requested) {
        AccessChannel channel = AccessChannel.parse(requested);
        if (channel == null) {
            channel = defaultChannel;
        }
        if (!hasChannelPolicy()) {
            return channel;
        }
        if (channel == null) {
            throw new BusinessException(
                    "ACCESS_CHANNEL_REQUIRED",
                    "Canal d'accès requis (MOBILE, WEB, GAB, USSD, BACKOFFICE)",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!supportedChannels.contains(channel)) {
            throw new BusinessException(
                    "ACCESS_CHANNEL_UNSUPPORTED",
                    "Canal " + channel + " non supporté pour ce journey (autorisés: " + supportedChannels + ")",
                    HttpStatus.BAD_REQUEST
            );
        }
        return channel;
    }

    /**
     * Résout l'audience (request → défaut) et valide {@code supportedAudiences} si déclarés.
     */
    public ProductAudience resolveAudience(String requested) {
        ProductAudience audience = ProductAudience.parse(requested);
        if (audience == null) {
            audience = defaultAudience;
        }
        if (!hasAudiencePolicy()) {
            return audience;
        }
        if (audience == null) {
            throw new BusinessException(
                    "PRODUCT_AUDIENCE_REQUIRED",
                    "Audience requise (RETAIL, CORPORATE, AGENT, MERCHANT, BACKOFFICE)",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!supportedAudiences.contains(audience)) {
            throw new BusinessException(
                    "PRODUCT_AUDIENCE_UNSUPPORTED",
                    "Audience " + audience + " non supportée pour ce produit (autorisées: "
                            + supportedAudiences + ")",
                    HttpStatus.BAD_REQUEST
            );
        }
        return audience;
    }

    /**
     * Configuration effective = base + overlay channel (bindings formKey, softRules, params).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> effectiveConfiguration(AccessChannel channel) {
        Map<String, Object> effective = new LinkedHashMap<>(configuration);
        if (channel == null) {
            return effective;
        }
        Map<String, Object> overlay = channelDefaults.get(channel.name());
        if (overlay == null || overlay.isEmpty()) {
            return effective;
        }

        if (overlay.get("bindings") instanceof List<?> overlayBindings) {
            effective.put("bindings", mergeBindings(
                    asMapList(effective.get("bindings")),
                    asMapList(overlayBindings)
            ));
        }
        if (overlay.get("softRules") instanceof List<?> soft) {
            effective.put("softRules", soft);
        }
        if (overlay.get("gateway") instanceof Map<?, ?> gw) {
            Map<String, Object> baseGw = effective.get("gateway") instanceof Map<?, ?> m
                    ? new LinkedHashMap<>((Map<String, Object>) m)
                    : new LinkedHashMap<>();
            baseGw.putAll((Map<String, Object>) gw);
            effective.put("gateway", baseGw);
        }
        if (overlay.get("copyRequestFields") instanceof List<?> fields) {
            Map<String, Object> baseGw = effective.get("gateway") instanceof Map<?, ?> m
                    ? new LinkedHashMap<>((Map<String, Object>) m)
                    : new LinkedHashMap<>();
            baseGw.put("copyRequestFields", fields);
            effective.put("gateway", baseGw);
        }
        return effective;
    }

    private static List<Map<String, Object>> mergeBindings(
            List<Map<String, Object>> base,
            List<Map<String, Object>> overlay
    ) {
        Map<String, Map<String, Object>> byStep = new LinkedHashMap<>();
        for (Map<String, Object> b : base) {
            String step = str(b.get("step"));
            if (step != null) {
                byStep.put(step.toUpperCase(Locale.ROOT), new LinkedHashMap<>(b));
            }
        }
        for (Map<String, Object> o : overlay) {
            String step = str(o.get("step"));
            if (step == null) {
                continue;
            }
            String key = step.toUpperCase(Locale.ROOT);
            Map<String, Object> merged = byStep.containsKey(key)
                    ? new LinkedHashMap<>(byStep.get(key))
                    : new LinkedHashMap<>();
            merged.putAll(o);
            byStep.put(key, merged);
        }
        return new ArrayList<>(byStep.values());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                out.add(new LinkedHashMap<>((Map<String, Object>) m));
            }
        }
        return out;
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
        return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
    }
}
