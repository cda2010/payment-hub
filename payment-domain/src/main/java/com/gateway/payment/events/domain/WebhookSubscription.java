package com.gateway.payment.events.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_subscription", indexes = {
        @Index(name = "idx_webhook_subscription_tenant", columnList = "tenant_id, active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookSubscription {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    /** Secret HMAC chiffré (même crypto que les webhooks inbound). */
    @Column(name = "secret_enc", columnDefinition = "TEXT")
    private String secretEnc;

    /** Liste CSV de types d'événements, ou {@code *} pour tous. */
    @Column(name = "event_types", nullable = false, length = 500)
    @Builder.Default
    private String eventTypes = "*";

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (eventTypes == null || eventTypes.isBlank()) {
            eventTypes = "*";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean matches(String eventType) {
        if (eventTypes == null || "*".equals(eventTypes.trim())) {
            return true;
        }
        for (String part : eventTypes.split(",")) {
            if (part.trim().equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }
}
