package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "limit_counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitCounter {

    @Id
    private UUID id;

    @Column(name = "limit_code", nullable = false, length = 64)
    private String limitCode;

    @Column(name = "scope_key", nullable = false, length = 200)
    private String scopeKey;

    @Column(name = "period_key", nullable = false, length = 64)
    private String periodKey;

    @Column(name = "amount_sum", nullable = false, precision = 18, scale = 4)
    private BigDecimal amountSum;

    @Column(name = "count_sum", nullable = false)
    private int countSum;

    @Column(name = "reserved_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal reservedAmount;

    @Column(name = "reserved_count", nullable = false)
    private int reservedCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (amountSum == null) {
            amountSum = BigDecimal.ZERO;
        }
        if (reservedAmount == null) {
            reservedAmount = BigDecimal.ZERO;
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public BigDecimal projectedAmount(BigDecimal extra) {
        return amountSum.add(reservedAmount).add(extra == null ? BigDecimal.ZERO : extra);
    }

    public int projectedCount(int extra) {
        return countSum + reservedCount + extra;
    }
}
