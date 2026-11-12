package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "limit_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitDefinition {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LimitPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LimitScope scope;

    @Column(nullable = false, length = 32)
    private String metric;

    @Column(name = "limit_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal limitValue;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "transaction_type", length = 64)
    private String transactionType;

    @Column(name = "kyc_level", length = 32)
    private String kycLevel;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (currency == null) {
            currency = "XOF";
        }
        if (metric == null) {
            metric = "AMOUNT";
        }
    }
}
