package com.gateway.payment.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "content.cache")
public record ContentCacheProperties(
        boolean redisEnabled,
        long caffeineMaxSize,
        long caffeineTtlSeconds,
        long redisTtlSeconds
) {
    public ContentCacheProperties {
        if (caffeineMaxSize <= 0) {
            caffeineMaxSize = 500;
        }
        if (caffeineTtlSeconds <= 0) {
            caffeineTtlSeconds = 60;
        }
        if (redisTtlSeconds <= 0) {
            redisTtlSeconds = 300;
        }
    }
}
