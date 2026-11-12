package com.gateway.payment.transfer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_fee_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferFeePolicy {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private FeePolicyType policyType;

    @Column(name = "country_code", nullable = false, length = 5)
    @Builder.Default
    private String countryCode = "BF";

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "XOF";

    @Column(name = "emitter_code", length = 50)
    private String emitterCode;

    @Column(name = "beneficiary_code", length = 50)
    private String beneficiaryCode;

    /** Null = applicable à tous les rails (fallback) */
    @Column(name = "rail_code", length = 40)
    private String railCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_rule_id", nullable = false)
    private FeeRule feeRule;

    @Column(nullable = false)
    private boolean actif;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordre = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
