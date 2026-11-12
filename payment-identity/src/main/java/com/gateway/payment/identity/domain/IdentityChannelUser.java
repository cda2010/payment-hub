package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Utilisateur canal — login métier lié à une Party (profil).
 * Le mot de passe peut rester chez l'IdP ; hash optionnel pour bootstrap local.
 */
@Entity
@Table(name = "identity_channel_user", indexes = {
        @Index(name = "idx_identity_user_username", columnList = "tenant_id, username", unique = true),
        @Index(name = "idx_identity_user_party", columnList = "party_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityChannelUser {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IdentityAudience audience;

    /** Rôle métier canal (GROUP_ADMIN, TREASURER, CASHIER, MAKER…). */
    @Column(nullable = false, length = 64)
    private String role;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

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
