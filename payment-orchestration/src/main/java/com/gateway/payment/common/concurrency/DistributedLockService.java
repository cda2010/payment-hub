package com.gateway.payment.common.concurrency;

/**
 * Lock distribué (mémoire ou Redis) pour idempotence / claims.
 */
public interface DistributedLockService {

    /**
     * Tente d'acquérir un lock.
     * @return true si acquis
     */
    boolean tryLock(String key, String owner, long ttlSeconds);

    void unlock(String key, String owner);

    /**
     * Stocke une valeur avec TTL (claims / cache léger).
     * @return true si la clé n'existait pas (SET NX)
     */
    boolean putIfAbsent(String key, String value, long ttlSeconds);

    String get(String key);

    void delete(String key);

    /** Set avec TTL (écrase). */
    void set(String key, String value, long ttlSeconds);
}
