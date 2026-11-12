package com.gateway.payment.billing.domain;

import com.gateway.payment.configuration.domain.PaymentProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "biller_product",
        uniqueConstraints = @UniqueConstraint(name = "uq_biller_product_code", columnNames = {"biller_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillerProduct {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biller_id", nullable = false)
    private Biller biller;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif;

    @Enumerated(EnumType.STRING)
    @Column(name = "lookup_mode", nullable = false, length = 30)
    @Builder.Default
    private LookupMode lookupMode = LookupMode.BOTH;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_policy", nullable = false, length = 30)
    @Builder.Default
    private AmountPolicy amountPolicy = AmountPolicy.FULL_ONLY;

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "allow_overpay", nullable = false)
    @Builder.Default
    private boolean allowOverpay = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "journey_code", nullable = false, length = 50)
    @Builder.Default
    private JourneyCode journeyCode = JourneyCode.OPEN_BILLS;

    @Column(name = "payment_flow_id")
    private UUID paymentFlowId;

    /**
     * Fournisseur FACTURIER spécifique au produit (optionnel).
     * Si null, on hérite du {@link Biller#getProvider()}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private PaymentProvider provider;

    /** Code FSE omnicanal propre au produit. Vide : hérite du facturier. */
    @Column(name = "provider_code", length = 40)
    private String providerCode;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @Column(name = "lookup_fields_json", columnDefinition = "TEXT")
    private String lookupFieldsJson;

    @Column(name = "confirm_fields_json", columnDefinition = "TEXT")
    private String confirmFieldsJson;

    @Column(name = "amount_fields_json", columnDefinition = "TEXT")
    private String amountFieldsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Provider effectif : produit si défini, sinon facturier. */
    public PaymentProvider resolvedProvider() {
        if (provider != null) {
            return provider;
        }
        return biller != null ? biller.getProvider() : null;
    }

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
