package com.gateway.payment.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_callback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallback {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @Column(name = "payment_id", length = 64)
    private String paymentId;

    @Column(name = "external_event_id", length = 150)
    private String externalEventId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean processed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
