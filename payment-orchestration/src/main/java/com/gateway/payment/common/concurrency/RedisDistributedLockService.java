package com.gateway.payment.common.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * Locks / claims Redis (SET NX + TTL) pour scale multi-pods.
 */
@Service
@ConditionalOnProperty(name = "gateway.performance.redis-enabled", havingValue = "true")
public class RedisDistributedLockService implements DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockService.class);

    private final StringRedisTemplate redis;

    public RedisDistributedLockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryLock(String key, String owner, long ttlSeconds) {
        return putIfAbsent(key, owner != null ? owner : "default", ttlSeconds);
    }

    @Override
    public void unlock(String key, String owner) {
        try {
            String current = redis.opsForValue().get(key);
            if (current != null && (owner == null || owner.equals(current))) {
                redis.delete(key);
            }
        } catch (Exception ex) {
            log.warn("Redis unlock failed key={}: {}", key, ex.getMessage());
        }
    }

    @Override
    public boolean putIfAbsent(String key, String value, long ttlSeconds) {
        try {
            Boolean ok = redis.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(Math.max(1, ttlSeconds)));
            return Boolean.TRUE.equals(ok);
        } catch (Exception ex) {
            log.warn("Redis putIfAbsent failed key={}: {}", key, ex.getMessage());
            return false;
        }
    }

    @Override
    public String get(String key) {
        try {
            return redis.opsForValue().get(key);
        } catch (Exception ex) {
            log.warn("Redis get failed key={}: {}", key, ex.getMessage());
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redis.delete(Objects.requireNonNull(key));
        } catch (Exception ex) {
            log.warn("Redis delete failed key={}: {}", key, ex.getMessage());
        }
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        try {
            redis.opsForValue().set(key, value, Duration.ofSeconds(Math.max(1, ttlSeconds)));
        } catch (Exception ex) {
            log.warn("Redis set failed key={}: {}", key, ex.getMessage());
        }
    }
}
