package com.gateway.payment.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stockage local des médias uploadés.
 * En prod, {@code public-base-url} peut pointer vers un CDN ; les fichiers restent
 * référencés par URL dans {@code content_item.image_url}.
 */
@ConfigurationProperties(prefix = "content.media")
public record ContentMediaProperties(
        String storageDir,
        String publicBaseUrl,
        long maxSizeBytes
) {
    public ContentMediaProperties {
        if (storageDir == null || storageDir.isBlank()) {
            storageDir = "./data/content-media";
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://127.0.0.1:8098/api/v1/content/media";
        }
        if (maxSizeBytes <= 0) {
            maxSizeBytes = 2_000_000L;
        }
    }
}
