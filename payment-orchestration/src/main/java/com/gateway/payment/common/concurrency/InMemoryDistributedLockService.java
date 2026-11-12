package com.gateway.payment.common.concurrency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallback single-node quand Redis est désactivé.
 */
@Service
@ConditionalOnProperty(name = "gateway.performance.redis-enabled", havingValue = "false", matchIfMissing = false)
public class InMemoryDistributedLockService implements DistributedLockService {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private record Entry(String value, Instant expiresAt) {
        boolean valid() {
            return expiresAt == null || expiresAt.isAfter(Instant.now());
        }
    }

    @Override
    public boolean tryLock(String key, String owner, long ttlSeconds) {
        return putIfAbsent(key, owner != null ? owner : "default", ttlSeconds);
    }

    @Override
    public void unlock(String key, String owner) {
        Entry current = store.get(key);
        if (current != null && (owner == null || owner.equals(current.value()))) {
            store.remove(key);
        }
    }

    @Override
    public boolean putIfAbsent(String key, String value, long ttlSeconds) {
        purgeExpired(key);
        Entry created = new Entry(value, Instant.now().plusSeconds(Math.max(1, ttlSeconds)));
        Entry prev = store.putIfAbsent(key, created);
        if (prev == null) {
            return true;
        }
        if (!prev.valid()) {
            return store.replace(key, prev, created);
        }
        return false;
    }

    @Override
    public String get(String key) {
        purgeExpired(key);
        Entry e = store.get(key);
        return e != null && e.valid() ? e.value() : null;
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        store.put(key, new Entry(value, Instant.now().plusSeconds(Math.max(1, ttlSeconds))));
    }

    private void purgeExpired(String key) {
        Entry e = store.get(key);
        if (e != null && !e.valid()) {
            store.remove(key, e);
        }
    }
}
