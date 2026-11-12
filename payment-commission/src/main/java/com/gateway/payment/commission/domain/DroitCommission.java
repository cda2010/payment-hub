package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commission_droit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroitCommission {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "transaction_id", nullable = false, length = 64)
    private String transactionId;

    @Column(name = "grille_id")
    private UUID grilleId;

    @Column(name = "grille_code", length = 64)
    private String grilleCode;

    @Column(name = "grille_version")
    private Integer grilleVersion;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "rule_code", length = 64)
    private String ruleCode;

    @Column(name = "beneficiary_role", nullable = false, length = 64)
    private String beneficiaryRole;

    @Column(name = "beneficiary_ref", length = 120)
    private String beneficiaryRef;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XOF";

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "funding_type", length = 32)
    private String fundingType;

    @Column(name = "funder_role", length = 64)
    private String funderRole;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "ACCRUED";

    @Column(name = "liquidation_id")
    private UUID liquidationId;

    @Column(length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "liquidated_at")
    private Instant liquidatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = "ACCRUED";
        }
    }
}
