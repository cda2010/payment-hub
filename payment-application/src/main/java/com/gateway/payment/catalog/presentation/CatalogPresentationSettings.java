package com.gateway.payment.catalog.presentation;

import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.datasources.CatalogDataOrigin;
import com.gateway.payment.datasources.CatalogDataSource;
import com.gateway.payment.datasources.DataSourceSection;
import com.gateway.payment.datasources.StaticCatalogItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bloc {@code presentation} de la configuration catalogue produit.
 * <pre>
 * configuration.presentation.sections[]
 * configuration.channelDefaults.MOBILE.presentation (overlay)
 * </pre>
 */
public final class CatalogPresentationSettings {

    private final int version;
    private final List<SectionDef> sections;

    private CatalogPresentationSettings(int version, List<SectionDef> sections) {
        this.version = version;
        this.sections = List.copyOf(sections);
    }

    public int version() {
        return version;
    }

    public List<SectionDef> sections() {
        return sections;
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static CatalogPresentationSettings from(Map<String, Object> configuration) {
        return from(configuration, null);
    }

    @SuppressWarnings("unchecked")
    public static CatalogPresentationSettings from(Map<String, Object> configuration, AccessChannel channel) {
        Map<String, Object> cfg = configuration != null
                ? new LinkedHashMap<>(configuration)
                : new LinkedHashMap<>();

        Map<String, Object> presentation = asMap(cfg.get("presentation"));
        if (channel != null) {
            Map<String, Object> overlays = asMap(cfg.get("channelDefaults"));
            if (overlays.isEmpty()) {
                overlays = asMap(cfg.get("channel_defaults"));
            }
            Map<String, Object> channelOverlay = asMap(overlays.get(channel.name()));
            Map<String, Object> presentationOverlay = asMap(channelOverlay.get("presentation"));
            if (!presentationOverlay.isEmpty()) {
                presentation = mergePresentation(presentation, presentationOverlay);
            }
        }

        CatalogDataOrigin defaultOrigin = CatalogDataOrigin.parse(
                str(first(presentation, "defaultOrigin", "default_origin", "origin"))
        );

        int version = 1;
        Object v = presentation.get("version");
        if (v instanceof Number n) {
            version = n.intValue();
        } else if (v != null) {
            try {
                version = Integer.parseInt(String.valueOf(v).trim());
            } catch (NumberFormatException ignored) {
                // keep 1
            }
        }

        List<SectionDef> sections = new ArrayList<>();
        Object rawSections = presentation.get("sections");
        if (rawSections instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?>)) {
                    continue;
                }
                SectionDef def = SectionDef.from((Map<String, Object>) item, defaultOrigin);
                if (def != null) {
                    sections.add(def);
                }
            }
        }
        return new CatalogPresentationSettings(version, sections);
    }

    /** Layout défaut BILL_PAYMENT si config absente — structure vide, pas de facturiers hardcodés. */
    public static CatalogPresentationSettings defaultBillPayment() {
        return new CatalogPresentationSettings(1, List.of(
                new SectionDef(
                        "favorites",
                        DisplayKind.SHORTCUT_SLIDE,
                        CatalogDataSource.BILL_FAVORITES,
                        CatalogDataSource.BILL_FAVORITES.name(),
                        CatalogDataOrigin.LOCAL,
                        "Vos favoris",
                        null,
                        false,
                        false,
                        null,
                        Map.of("limit", 8),
                        List.of()
                ),
                new SectionDef(
                        "categories",
                        DisplayKind.FILTER_CHIPS,
                        CatalogDataSource.BILLER_CATEGORIES,
                        CatalogDataSource.BILLER_CATEGORIES.name(),
                        CatalogDataOrigin.LOCAL,
                        "Catégories",
                        "billers",
                        false,
                        false,
                        null,
                        Map.of(),
                        List.of()
                ),
                new SectionDef(
                        "billers",
                        DisplayKind.LEAF_LIST,
                        CatalogDataSource.BILLERS,
                        CatalogDataSource.BILLERS.name(),
                        CatalogDataOrigin.LOCAL,
                        null,
                        null,
                        true,
                        true,
                        30,
                        Map.of("size", 30),
                        List.of()
                )
        ));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergePresentation(Map<String, Object> base, Map<String, Object> overlay) {
        Map<String, Object> merged = new LinkedHashMap<>(base);
        merged.putAll(overlay);
        if (overlay.containsKey("sections")) {
            merged.put("sections", overlay.get("sections"));
        }
        return merged;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw) {
        if (raw instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return new LinkedHashMap<>();
    }

    public record SectionDef(
            String id,
            DisplayKind displayKind,
            CatalogDataSource dataSource,
            String dataSourceCode,
            CatalogDataOrigin origin,
            String title,
            String bindsTo,
            boolean searchable,
            boolean pageable,
            Integer pageSize,
            Map<String, Object> params,
            List<CatalogItemDef> staticItems
    ) {
        public DataSourceSection toDataSourceSection() {
            List<StaticCatalogItem> items = staticItems.stream()
                    .map(i -> new StaticCatalogItem(i.id(), i.label(), i.sub(), i.icon(), i.value(), i.meta()))
                    .toList();
            return new DataSourceSection(id, dataSourceCode, dataSource, origin, params, items, pageSize);
        }

        @SuppressWarnings("unchecked")
        static SectionDef from(Map<String, Object> raw, CatalogDataOrigin defaultOrigin) {
            String id = str(raw.get("id"));
            if (id == null) {
                id = str(raw.get("code"));
            }
            if (id == null) {
                return null;
            }
            DisplayKind kind = DisplayKind.parseOrLeaf(str(first(raw, "displayKind", "display_kind")));
            String rawSource = str(first(raw, "dataSource", "data_source", "dataRef", "data_ref"));
            CatalogDataSource source = CatalogDataSource.parse(rawSource);
            if (source == null) {
                source = CatalogDataSource.STATIC_ITEMS;
            }
            String dataSourceCode = rawSource != null
                    ? rawSource.trim().toUpperCase(Locale.ROOT).replace('-', '_')
                    : CatalogDataSource.STATIC_ITEMS.name();
            if (source == CatalogDataSource.CUSTOM && rawSource == null) {
                dataSourceCode = CatalogDataSource.CUSTOM.name();
            }

            CatalogDataOrigin origin = CatalogDataOrigin.parse(
                    str(first(raw, "origin", "dataOrigin", "data_origin"))
            );
            if (str(first(raw, "origin", "dataOrigin", "data_origin")) == null) {
                origin = defaultOrigin != null ? defaultOrigin : CatalogDataOrigin.LOCAL;
            }

            String title = str(first(raw, "title", "titleKey", "title_key"));
            String bindsTo = str(first(raw, "bindsTo", "binds_to"));
            boolean searchable = bool(raw.get("searchable"), kind == DisplayKind.LEAF_LIST);
            boolean pageable = bool(raw.get("pageable"), kind == DisplayKind.LEAF_LIST);
            Integer pageSize = null;
            Map<String, Object> params = asMap(raw.get("params"));
            Object size = params.get("size");
            if (size == null) {
                size = raw.get("size");
            }
            if (size instanceof Number n) {
                pageSize = n.intValue();
            }

            List<CatalogItemDef> items = new ArrayList<>();
            Object rawItems = raw.get("items");
            if (rawItems instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m) {
                        CatalogItemDef it = CatalogItemDef.from((Map<String, Object>) m);
                        if (it != null) {
                            items.add(it);
                        }
                    }
                }
            }
            return new SectionDef(
                    id,
                    kind,
                    source,
                    dataSourceCode,
                    origin,
                    title,
                    bindsTo,
                    searchable,
                    pageable,
                    pageSize,
                    Map.copyOf(params),
                    List.copyOf(items)
            );
        }
    }

    public record CatalogItemDef(
            String id,
            String label,
            String sub,
            String icon,
            String value,
            Map<String, Object> meta
    ) {
        @SuppressWarnings("unchecked")
        static CatalogItemDef from(Map<String, Object> raw) {
            String id = str(first(raw, "id", "code"));
            String label = str(first(raw, "label", "name"));
            if (id == null || label == null) {
                return null;
            }
            Map<String, Object> meta = asMap(raw.get("meta"));
            if (!meta.containsKey("code")) {
                meta = new LinkedHashMap<>(meta);
                meta.put("code", id);
            }
            return new CatalogItemDef(
                    id,
                    label,
                    str(raw.get("sub")),
                    str(raw.get("icon")),
                    str(raw.get("value")),
                    Map.copyOf(meta)
            );
        }
    }

    private static Object first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
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

    private static boolean bool(Object v, boolean defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
