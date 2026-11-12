package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Profil d'offre : gabarit de services catalogue ({@link ProfileKind#SERVICE}),
 * de grilles tarifaires ({@link ProfileKind#TARIFF}) ou de plafonds ({@link ProfileKind#LIMIT}).
 * S'affecte à une {@link IdentityParty} ou une unité réseau ; ce n'est pas un rôle d'administration.
 */
@Entity
@Table(name = "identity_profile", indexes = {
        @Index(name = "idx_identity_profile_tenant", columnList = "tenant_id"),
        @Index(name = "idx_identity_profile_kind", columnList = "tenant_id, kind")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_profile_tenant_code", columnNames = {"tenant_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityProfile {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(length = 500)
    private String description;

    /** SERVICE = produits autorisés ; TARIFF = grilles de frais ; LIMIT = groupes de plafonds. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ProfileKind kind = ProfileKind.SERVICE;

    /** Audience typique (RETAIL, AGENT…) ; informative, pas une contrainte d'affectation. */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private IdentityAudience audience;

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
