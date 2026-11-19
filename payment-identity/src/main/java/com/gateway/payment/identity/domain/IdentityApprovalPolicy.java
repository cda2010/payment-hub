package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Politique d'approbation d'un produit, portée par un nœud d'organisation. */
@Entity
@Table(name = "identity_approval_policy", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_approval_policy", columnNames = {"org_node_id", "product_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityApprovalPolicy {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "org_node_id", nullable = false)
    private UUID orgNodeId;

    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "override_flag", nullable = false)
    @Builder.Default
    private boolean overrideFlag = true;

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
