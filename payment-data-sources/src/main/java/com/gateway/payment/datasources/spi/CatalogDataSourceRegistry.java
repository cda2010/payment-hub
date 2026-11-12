package com.gateway.payment.datasources.spi;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Registre Spring des {@link CatalogDataSourceHandler} — un bean peut couvrir plusieurs codes.
 */
@Component
public class CatalogDataSourceRegistry {

    private final Map<String, CatalogDataSourceHandler> byCode;

    public CatalogDataSourceRegistry(List<CatalogDataSourceHandler> handlers) {
        Map<String, CatalogDataSourceHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (CatalogDataSourceHandler handler : handlers) {
                if (handler == null || handler.codes() == null) {
                    continue;
                }
                for (String raw : handler.codes()) {
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    String code = normalize(raw);
                    CatalogDataSourceHandler previous = map.put(code, handler);
                    if (previous != null && previous != handler) {
                        throw new IllegalStateException(
                                "DataSource en double: " + code + " (" + previous.getClass().getName()
                                        + " vs " + handler.getClass().getName() + ")"
                        );
                    }
                }
            }
        }
        this.byCode = Map.copyOf(map);
    }

    public Optional<CatalogDataSourceHandler> get(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCode.get(normalize(code)));
    }

    public boolean has(String code) {
        return get(code).isPresent();
    }

    /** Inventaire des codes enregistrés (ordre d'enregistrement). */
    public List<String> codes() {
        return List.copyOf(byCode.keySet());
    }

    public List<CatalogDataSourceHandler> handlers() {
        List<CatalogDataSourceHandler> unique = new ArrayList<>();
        for (CatalogDataSourceHandler h : byCode.values()) {
            if (!unique.contains(h)) {
                unique.add(h);
            }
        }
        return List.copyOf(unique);
    }

    public static String normalize(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
