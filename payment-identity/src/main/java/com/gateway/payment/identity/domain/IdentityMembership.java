package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Lien utilisateur ↔ nœud org + portée d'accès.
 */
@Entity
@Table(name = "identity_membership", indexes = {
        @Index(name = "idx_identity_membership_user", columnList = "user_id"),
        @Index(name = "idx_identity_membership_org", columnList = "org_node_id"),
        @Index(name = "idx_identity_membership_uniq", columnList = "user_id, org_node_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityMembership {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "org_node_id", nullable = false)
    private UUID orgNodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private AccessScope scope = AccessScope.DIRECT;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
