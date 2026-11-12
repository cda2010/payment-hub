package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "payment_flow",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_flow_provider_service_type", columnNames = {"provider_id", "service_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFlow {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    /**
     * Type de service orchestré par ce flow (1 couple provider + serviceType).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    private ServiceTypeCode serviceType;

    /**
     * Clé de définition Flowable ({@code process id} BPMN).
     * Quand renseignée et {@code gateway.orchestration.mode=flowable}, le checkout
     * démarre cette définition (résolution provider + serviceType → flow).
     */
    @Column(name = "bpm_process_key", length = 100)
    private String bpmProcessKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<PaymentFlowStep> steps = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
