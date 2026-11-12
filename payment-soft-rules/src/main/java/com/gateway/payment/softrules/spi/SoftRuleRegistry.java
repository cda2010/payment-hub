package com.gateway.payment.softrules.spi;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Registre Spring des {@link SoftRuleHandler} — un bean peut couvrir plusieurs types.
 */
@Component
public class SoftRuleRegistry {

    private final Map<String, SoftRuleHandler> byType;

    public SoftRuleRegistry(List<SoftRuleHandler> handlers) {
        Map<String, SoftRuleHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (SoftRuleHandler handler : handlers) {
                if (handler == null || handler.types() == null) {
                    continue;
                }
                for (String raw : handler.types()) {
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    String type = normalize(raw);
                    SoftRuleHandler previous = map.put(type, handler);
                    if (previous != null && previous != handler) {
                        throw new IllegalStateException(
                                "SoftRule type en double: " + type + " (" + previous.getClass().getName()
                                        + " vs " + handler.getClass().getName() + ")"
                        );
                    }
                }
            }
        }
        this.byType = Map.copyOf(map);
    }

    public Optional<SoftRuleHandler> get(String type) {
        if (type == null || type.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byType.get(normalize(type)));
    }

    public boolean has(String type) {
        return get(type).isPresent();
    }

    public List<String> types() {
        return List.copyOf(byType.keySet());
    }

    public List<SoftRuleHandler> handlers() {
        List<SoftRuleHandler> unique = new ArrayList<>();
        for (SoftRuleHandler h : byType.values()) {
            if (!unique.contains(h)) {
                unique.add(h);
            }
        }
        return List.copyOf(unique);
    }

    public static String normalize(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    /** Construction hors Spring (tests) avec la liste fournie. */
    public static SoftRuleRegistry of(List<SoftRuleHandler> handlers) {
        return new SoftRuleRegistry(handlers);
    }
}
