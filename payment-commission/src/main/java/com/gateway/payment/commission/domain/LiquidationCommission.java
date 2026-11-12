package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commission_liquidation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidationCommission {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "beneficiary_role", nullable = false, length = 64)
    private String beneficiaryRole;

    @Column(name = "beneficiary_ref", length = 120)
    private String beneficiaryRef;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XOF";

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "droit_count", nullable = false)
    @Builder.Default
    private int droitCount = 0;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
