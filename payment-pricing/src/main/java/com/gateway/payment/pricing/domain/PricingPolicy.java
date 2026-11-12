package com.gateway.payment.pricing.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Binding produit / canal / audience → taux et payeur des frais.
 * Les frais viennent des grilles tarifaires ; les plafonds sont dans payment-rules.
 * Priorité de matching : plus de dimensions renseignées = plus spécifique.
 */
@Entity
@Table(name = "pricing_policy", indexes = {
        @Index(name = "idx_pricing_policy_product", columnList = "product_code, active"),
        @Index(name = "uq_pricing_policy_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPolicy {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "product_code", length = 80)
    private String productCode;

    @Column(length = 32)
    private String channel;

    @Column(length = 32)
    private String audience;

    @Column(name = "country_code", length = 8)
    private String countryCode;

    @Column(name = "rail_code", length = 40)
    private String railCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_bearer", nullable = false, length = 20)
    @Builder.Default
    private PricingFeeBearer feeBearer = PricingFeeBearer.SENDER;

    @Column(name = "rate_code", length = 64)
    private String rateCode;

    /** Plus élevé = prioritaire. */
    @Column(nullable = false)
    @Builder.Default
    private int priority = 100;

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
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
