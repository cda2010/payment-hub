package com.gateway.payment.documents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documents.storage")
public record DocumentStorageProperties(
        String dir,
        long maxSizeBytes,
        String publicBaseUrl
) {
    public DocumentStorageProperties {
        if (dir == null || dir.isBlank()) {
            dir = "./data/documents";
        }
        if (maxSizeBytes <= 0) {
            maxSizeBytes = 10 * 1024 * 1024L;
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://127.0.0.1:8101/api/v1/documents";
        }
    }
}
