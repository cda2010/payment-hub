package com.gateway.payment.configuration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderApi {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private ProviderEnvironment environment;

    /** Api Omnicanal parente (Methode → Api). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fse_api_id")
    private ProviderFseApi fseApi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private ProviderOperation operation;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "http_method", nullable = false, length = 20)
    private String httpMethod;

    @Column(nullable = false)
    private String path;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "request_mapping_json", columnDefinition = "TEXT")
    private String requestMappingJson;

    @Column(name = "response_mapping_json", columnDefinition = "TEXT")
    private String responseMappingJson;

    @Column(name = "retry_enabled", nullable = false)
    private boolean retryEnabled;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "retry_delay_ms")
    private Long retryDelayMs;

    @Column(name = "backoff_multiplier")
    private Double backoffMultiplier;

    @Column(name = "retry_on")
    private String retryOn;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
