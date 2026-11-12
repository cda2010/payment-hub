package com.gateway.payment.schedule.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "scheduled_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPayment {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "product_code", nullable = false, length = 80)
    private String productCode;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScheduledPaymentFrequency frequency;

    @Column(name = "next_run_at", nullable = false)
    private LocalDate nextRunAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScheduledPaymentStatus status = ScheduledPaymentStatus.ACTIVE;

    @Column(name = "destination_provider", length = 50)
    private String destinationProvider;

    @Column(name = "dest_phone", length = 30)
    private String destPhone;

    @Column(name = "dest_account", length = 64)
    private String destAccount;

    @Column(name = "beneficiary_label", length = 150)
    private String beneficiaryLabel;

    @Column(name = "biller_code", length = 80)
    private String billerCode;

    @Column(name = "subscriber_reference", length = 120)
    private String subscriberReference;

    @Column(name = "source_rail", length = 50)
    private String sourceRail;

    @Column(name = "source_payment_order_id", length = 80)
    private String sourcePaymentOrderId;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "owner_key", length = 80)
    private String ownerKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
