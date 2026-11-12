package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "limit_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitReservation {

    @Id
    private UUID id;

    @Column(name = "transaction_id", nullable = false, length = 80)
    private String transactionId;

    @Column(name = "limit_code", nullable = false, length = 64)
    private String limitCode;

    @Column(name = "scope_key", nullable = false, length = 200)
    private String scopeKey;

    @Column(name = "period_key", nullable = false, length = 64)
    private String periodKey;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "count_units", nullable = false)
    private int countUnits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = ReservationStatus.HELD;
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (countUnits <= 0) {
            countUnits = 1;
        }
    }
}
