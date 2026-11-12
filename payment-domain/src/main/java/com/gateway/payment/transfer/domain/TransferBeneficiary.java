package com.gateway.payment.transfer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Bénéficiaire enregistré pour les transferts (carnet mobile). */
@Entity
@Table(name = "transfer_beneficiary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferBeneficiary {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(length = 30)
    private String phone;

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    /** Code agence (banque, ex. Coris Bank). */
    @Column(name = "agency_code", length = 20)
    private String agencyCode;

    /** Code participant destination (ex. ORANGE_MONEY, CORIS_BANK). */
    @Column(name = "destination_provider", nullable = false, length = 50)
    private String destinationProvider;

    @Column(length = 60)
    private String icon;

    /** Clé propriétaire (client / device) — démo = {@code demo}. */
    @Column(name = "owner_key", length = 80)
    private String ownerKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
