package com.gateway.payment.content.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.content.api.dto.ContentDtos.ContentCardDto;
import com.gateway.payment.content.config.ContentCacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Cache à deux niveaux pour le hot path mobile {@code GET /content?slot=} :
 * <ol>
 *   <li>Caffeine (L1, process-local)</li>
 *   <li>Redis (L2, partagé entre instances) — optionnel</li>
 * </ol>
 */
@Component
public class PublishedContentCache {

    private static final Logger log = LoggerFactory.getLogger(PublishedContentCache.class);
    private static final String KEY_PREFIX = "content:published:";
    private static final TypeReference<List<ContentCardDto>> LIST_TYPE = new TypeReference<>() {
    };

    private final Cache<String, List<ContentCardDto>> local;
    private final ContentCacheProperties properties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redis;

    public PublishedContentCache(
            ContentCacheProperties properties,
            ObjectMapper objectMapper,
            ObjectProvider<StringRedisTemplate> redis
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.redis = properties.redisEnabled() ? redis.getIfAvailable() : null;
        this.local = Caffeine.newBuilder()
                .maximumSize(properties.caffeineMaxSize())
                .expireAfterWrite(Duration.ofSeconds(properties.caffeineTtlSeconds()))
                .build();
        if (properties.redisEnabled() && this.redis == null) {
            log.warn("content.cache.redis-enabled=true mais StringRedisTemplate absent — L2 désactivé");
        }
    }

    public List<ContentCardDto> getOrLoad(
            String slot,
            String locale,
            String channel,
            String audience,
            Supplier<List<ContentCardDto>> loader
    ) {
        String key = cacheKey(slot, locale, channel, audience);

        List<ContentCardDto> hit = local.getIfPresent(key);
        if (hit != null) {
            return hit;
        }

        hit = getFromRedis(key);
        if (hit != null) {
            local.put(key, hit);
            return hit;
        }

        List<ContentCardDto> loaded = List.copyOf(loader.get());
        local.put(key, loaded);
        putRedis(key, loaded);
        return loaded;
    }

    /** Invalide toutes les locales d’un slot (après create/update/delete). */
    public void evictSlot(String slot) {
        if (slot == null || slot.isBlank()) {
            evictAll();
            return;
        }
        String prefix = KEY_PREFIX + slot.trim() + ":";
        local.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        if (redis != null) {
            try {
                Set<String> keys = redis.keys(prefix + "*");
                if (keys != null && !keys.isEmpty()) {
                    redis.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Échec invalidation Redis slot={}: {}", slot, e.toString());
            }
        }
    }

    public void evictAll() {
        local.invalidateAll();
        if (redis != null) {
            try {
                Set<String> keys = redis.keys(KEY_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redis.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Échec invalidation Redis globale: {}", e.toString());
            }
        }
    }

    private List<ContentCardDto> getFromRedis(String key) {
        if (redis == null) {
            return null;
        }
        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, LIST_TYPE);
        } catch (Exception e) {
            log.warn("Lecture Redis échouée key={}: {}", key, e.toString());
            return null;
        }
    }

    private void putRedis(String key, List<ContentCardDto> value) {
        if (redis == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, Duration.ofSeconds(properties.redisTtlSeconds()));
        } catch (Exception e) {
            log.warn("Écriture Redis échouée key={}: {}", key, e.toString());
        }
    }

    private static String cacheKey(String slot, String locale, String channel, String audience) {
        return KEY_PREFIX + slot + ":" + locale
                + ":" + (channel == null || channel.isBlank() ? "_" : channel.trim().toUpperCase())
                + ":" + (audience == null || audience.isBlank() ? "_" : audience.trim().toUpperCase());
    }
}
