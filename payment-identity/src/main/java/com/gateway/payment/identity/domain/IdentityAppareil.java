package com.gateway.payment.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Téléphone ou navigateur autorisé à garder une session. Le code secret reste sur l'appareil. */
@Entity
@Table(name = "identity_appareil",
        uniqueConstraints = @UniqueConstraint(name = "uq_identity_appareil", columnNames = {"tenant_id", "appareil_id"}),
        indexes = @Index(name = "idx_identity_appareil_party", columnList = "party_id"))
@Getter
@Setter
@NoArgsConstructor
public class IdentityAppareil {

    public enum Plateforme {
        IOS, ANDROID, WEB
    }

    public enum Statut {
        ACTIF, REVOQUE
    }

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "appareil_id", nullable = false, length = 80)
    private String appareilId;

    @Column(nullable = false, length = 120)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Plateforme plateforme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Statut statut;

    @Column(name = "session_id", length = 80)
    private String sessionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
