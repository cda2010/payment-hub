package com.gateway.payment.payment.domain;

import com.gateway.payment.catalog.domain.CatalogDeServiceVersion;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.PaymentStatusTransitionGuard;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    /** Institution / tenant (claim JWT {@code tenant_id}). Null = txn legacy. */
    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    /** Type de service produit (LINK, TRANSFERT, PAYEMENT, PAIEMENT_FACTURE…). */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    @Builder.Default
    private ServiceTypeCode serviceType = ServiceTypeCode.PAYEMENT;

    /**
     * Version exacte du catalogue de service utilisée pour cette transaction
     * (snapshot immuable : ex. ACCOUNT_TRANSFER v1.1).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_service_version_id")
    private CatalogDeServiceVersion catalogServiceVersion;

    @Column(name = "merchant_reference", length = 100)
    private String merchantReference;

    @Column(name = "customer_reference", length = 100)
    private String customerReference;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Setter(AccessLevel.NONE)
    private PaymentStatus status;

    /**
     * Transition contrôlée. No-op si même statut ou si déjà terminal
     * (idempotence webhooks / double finalisation).
     */
    public boolean applyStatus(PaymentStatus next) {
        if (next == null || next == this.status) {
            return false;
        }
        if (PaymentStatusTransitionGuard.isTerminal(this.status)) {
            return false;
        }
        if (!PaymentStatusTransitionGuard.canTransition(this.status, next)) {
            throw new BusinessException(
                    "INVALID_STATUS_TRANSITION",
                    "Transition interdite: " + this.status + " → " + next,
                    HttpStatus.CONFLICT
            );
        }
        this.status = next;
        return true;
    }

    /**
     * Affectation sans garde (hydratation / copie d'état entre instances).
     * Ne pas utiliser pour une transition métier.
     */
    public void hydrateStatus(PaymentStatus status) {
        this.status = status;
    }

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "context_json", columnDefinition = "TEXT")
    private String contextJson;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (serviceType == null) {
            serviceType = ServiceTypeCode.PAYEMENT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
