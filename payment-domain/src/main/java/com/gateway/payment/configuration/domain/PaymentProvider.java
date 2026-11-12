package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "payment_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProvider {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", nullable = false, length = 30)
    @Builder.Default
    private PaymentMethodType paymentMethodType = PaymentMethodType.MOBILE_MONEY;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_execution", nullable = false, length = 50)
    private ExecutionMode modeExecution;

    /**
     * Types de service supportés (LINK, TRANSFERT, PAYEMENT…).
     * Aligné catalogue Omnicanal {@code ServiceCapability} / type de service produit.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "provider_service_type",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "service_type_code")
    )
    @Builder.Default
    private Set<ServiceType> serviceTypes = new HashSet<>();

    /** Code FSE Omnicanal (ex. 0011 Coris Money, 0004 Coris Banque). Nullable. */
    @Column(name = "code_fse", length = 20)
    private String codeFse;

    @Column(name = "code_externe", length = 50)
    private String codeExterne;

    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_kind", length = 20)
    private FseParticipantKind participantKind;

    @Enumerated(EnumType.STRING)
    @Column(name = "fse_statut", length = 20)
    private FseStatut fseStatut;

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
