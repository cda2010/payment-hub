package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidframe.designer.CheckoutFormDesignerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formulaires checkout rattaches aux userTasks via flowable formKey.
 * Ordre de résolution : override concepteur (DB / fichier module) → classpath
 * {@code modules/.../forms/formKey.json}.
 */
@Component
public class CheckoutFormCatalog {

    private static final Logger log = LoggerFactory.getLogger(CheckoutFormCatalog.class);

    private final ObjectMapper objectMapper;
    private final ObjectProvider<CheckoutFormDesignerService> checkoutDesigner;
    private final ConcurrentHashMap<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public CheckoutFormCatalog(
            ObjectMapper objectMapper,
            ObjectProvider<CheckoutFormDesignerService> checkoutDesigner
    ) {
        this.objectMapper = objectMapper;
        this.checkoutDesigner = checkoutDesigner;
    }

    public Optional<Map<String, Object>> open(String formKey) {
        if (formKey == null || formKey.isBlank()) {
            return Optional.empty();
        }
        String key = formKey.trim();

        CheckoutFormDesignerService designer = checkoutDesigner.getIfAvailable();
        if (designer != null) {
            Optional<Map<String, Object>> fromDesigner = designer.loadRuntimeDocument(key);
            if (fromDesigner.isPresent()) {
                cache.put(key, fromDesigner.get());
                return fromDesigner;
            }
        }

        Map<String, Object> cached = cache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:/modules/*/forms/" + key + ".json");
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    Map<String, Object> body = objectMapper.readValue(in, new TypeReference<>() {});
                    cache.put(key, body);
                    return Optional.of(body);
                }
            }
        } catch (Exception e) {
            log.warn("Checkout form '{}' introuvable: {}", key, e.getMessage());
        }
        return Optional.empty();
    }

    public void invalidate(String formKey) {
        if (formKey != null && !formKey.isBlank()) {
            cache.remove(formKey.trim());
        }
    }

    public void invalidateAll() {
        cache.clear();
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> formioSchema(String formKey) {
        return open(formKey).flatMap(body -> {
            Object formio = body.get("formio");
            if (formio instanceof Map<?, ?> map) {
                return Optional.of((Map<String, Object>) map);
            }
            return Optional.empty();
        });
    }

    public Optional<String> gatewayNextAction(String formKey) {
        return open(formKey).flatMap(body -> {
            Object gateway = body.get("gateway");
            if (gateway instanceof Map<?, ?> map && map.get("nextAction") != null) {
                return Optional.of(String.valueOf(map.get("nextAction")));
            }
            return Optional.empty();
        });
    }
}
