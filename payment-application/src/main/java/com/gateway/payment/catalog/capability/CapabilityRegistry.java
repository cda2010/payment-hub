package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Registre Spring des {@link CapabilityHandler} — un bean par code.
 */
@Component
public class CapabilityRegistry {

    private final Map<String, CapabilityHandler> byCode;

    public CapabilityRegistry(List<CapabilityHandler> handlers) {
        Map<String, CapabilityHandler> map = new LinkedHashMap<>();
        for (CapabilityHandler handler : handlers) {
            if (handler == null || handler.code() == null || handler.code().isBlank()) {
                continue;
            }
            String code = handler.code().trim().toUpperCase(Locale.ROOT);
            CapabilityHandler previous = map.put(code, handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Capability en double: " + code + " (" + previous.getClass().getName()
                                + " vs " + handler.getClass().getName() + ")");
            }
        }
        this.byCode = Map.copyOf(map);
    }

    public Optional<CapabilityHandler> get(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCode.get(code.trim().toUpperCase(Locale.ROOT)));
    }

    public boolean has(String code) {
        return get(code).isPresent();
    }

    public Map<String, CapabilityHandler> all() {
        return byCode;
    }
}
