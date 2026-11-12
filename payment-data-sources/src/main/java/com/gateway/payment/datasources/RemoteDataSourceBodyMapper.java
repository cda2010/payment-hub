package com.gateway.payment.datasources;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mappe le JSON d'un Product Gateway peer vers {@link RemoteDataSourceItem}.
 * Accepte {@code items|data|results|content|establishments|billers}.
 */
final class RemoteDataSourceBodyMapper {

    private RemoteDataSourceBodyMapper() {}

    @SuppressWarnings("unchecked")
    static RemoteDataSourceResult mapBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return RemoteDataSourceResult.empty();
        }
        boolean hasMore = bool(body.get("hasMore")) || bool(body.get("has_more"));
        Object rawItems = first(body, "items", "data", "results", "content", "establishments", "billers", "participants");
        if (rawItems instanceof List<?> list) {
            return new RemoteDataSourceResult(mapItemList(list), hasMore);
        }
        RemoteDataSourceItem single = mapOneItem(body);
        if (single != null) {
            return new RemoteDataSourceResult(List.of(single), false);
        }
        return RemoteDataSourceResult.empty();
    }

    @SuppressWarnings("unchecked")
    private static List<RemoteDataSourceItem> mapItemList(List<?> list) {
        List<RemoteDataSourceItem> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                RemoteDataSourceItem item = mapOneItem((Map<String, Object>) m);
                if (item != null) {
                    out.add(item);
                }
            } else if (o != null) {
                String v = String.valueOf(o);
                out.add(new RemoteDataSourceItem(v, v, null, "receipt-outline", v, Map.of("code", v)));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static RemoteDataSourceItem mapOneItem(Map<String, Object> raw) {
        String id = firstNonBlank(
                str(raw.get("id")),
                str(raw.get("code")),
                str(raw.get("value")),
                str(raw.get("key"))
        );
        String label = firstNonBlank(
                str(raw.get("label")),
                str(raw.get("name")),
                str(raw.get("libelle")),
                id
        );
        if (!StringUtils.hasText(id) || !StringUtils.hasText(label)) {
            return null;
        }
        String sub = firstNonBlank(str(raw.get("sub")), str(raw.get("description")), str(raw.get("subtitle")));
        String icon = firstNonBlank(str(raw.get("icon")), "receipt-outline");
        String value = str(raw.get("value"));
        Map<String, Object> meta;
        Object rawMeta = raw.get("meta");
        if (rawMeta instanceof Map<?, ?> mm) {
            meta = new LinkedHashMap<>((Map<String, Object>) mm);
        } else {
            meta = new LinkedHashMap<>();
        }
        meta.putIfAbsent("code", id);
        return new RemoteDataSourceItem(id, label, sub, icon, value, Map.copyOf(meta));
    }

    private static Object first(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) {
                return map.get(k);
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

    private static boolean bool(Object v) {
        return v != null && Boolean.parseBoolean(String.valueOf(v));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
