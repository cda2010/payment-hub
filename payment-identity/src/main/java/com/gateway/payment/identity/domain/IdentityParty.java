package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Party — personne ou organisation (client, société, agent physique…).
 * Pas d'authentification ici : profil métier uniquement.
 */
@Entity
@Table(name = "identity_party", indexes = {
        @Index(name = "idx_identity_party_tenant_code", columnList = "tenant_id, code", unique = true),
        @Index(name = "idx_identity_party_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityParty {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PartyKind kind;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(length = 30)
    private String phone;

    @Column(length = 180)
    private String email;

    @Column(length = 40)
    private String externalRef;

    /** Niveau KYC métier (BASIC, STANDARD, ENHANCED…). */
    @Column(name = "kyc_level", nullable = false, length = 32)
    @Builder.Default
    private String kycLevel = "STANDARD";

    /** Statut client (ACTIVE, SUSPENDED, CLOSED…). */
    @Column(name = "customer_status", nullable = false, length = 32)
    @Builder.Default
    private String customerStatus = "ACTIVE";

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
