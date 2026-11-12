package com.gateway.payment.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.performance")
public class GatewayPerformanceProperties {

    /** Cache métier orchestration / catalogue (TTL secondes). */
    private int cacheTtlSeconds = 600;

    /** Taille max des caches Caffeine runtime. */
    private int cacheMaxSize = 2_000;

    /** Persistance audit payment_execution / payment_event en async. */
    private boolean asyncAudit = true;

    /**
     * Active Redis (locks idempotence, step claims).
     * Nécessite Redis joignable ; sinon fallback mémoire avec warning.
     */
    private boolean redisEnabled = true;

    /** TTL locks / claims Redis (secondes). */
    private int redisLockTtlSeconds = 120;

    /** Exécute en parallèle les jambes sans dépendance dans SpecDriven. */
    private boolean parallelLegs = true;

    /** Threads dédiés aux jambes parallèles (si virtual-threads=false). */
    private int legExecutorThreads = 64;

    /**
     * Virtual threads (Java 21+) pour Tomcat + HTTP connecteurs + jambes.
     * Désactivé automatiquement sous Java &lt; 21.
     */
    private boolean virtualThreads = true;

    private final Http http = new Http();

    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(int cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    public boolean isAsyncAudit() {
        return asyncAudit;
    }

    public void setAsyncAudit(boolean asyncAudit) {
        this.asyncAudit = asyncAudit;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public int getRedisLockTtlSeconds() {
        return redisLockTtlSeconds;
    }

    public void setRedisLockTtlSeconds(int redisLockTtlSeconds) {
        this.redisLockTtlSeconds = redisLockTtlSeconds;
    }

    public boolean isParallelLegs() {
        return parallelLegs;
    }

    public void setParallelLegs(boolean parallelLegs) {
        this.parallelLegs = parallelLegs;
    }

    public int getLegExecutorThreads() {
        return legExecutorThreads;
    }

    public void setLegExecutorThreads(int legExecutorThreads) {
        this.legExecutorThreads = legExecutorThreads;
    }

    public boolean isVirtualThreads() {
        return virtualThreads;
    }

    public void setVirtualThreads(boolean virtualThreads) {
        this.virtualThreads = virtualThreads;
    }

    public Http getHttp() {
        return http;
    }

    public static class Http {
        private int connectTimeoutMs = 3_000;
        private int readTimeoutMs = 30_000;
        private int executorThreads = 200;
        /** Connexions max par host FSE (pool per-route). */
        private int maxConnectionsPerRoute = 50;

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public int getExecutorThreads() {
            return executorThreads;
        }

        public void setExecutorThreads(int executorThreads) {
            this.executorThreads = executorThreads;
        }

        public int getMaxConnectionsPerRoute() {
            return maxConnectionsPerRoute;
        }

        public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        }
    }
}
