package com.gateway.payment.alias.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Alias de compte — pointeur stable vers un compte CBS / Coris / till.
 * Ne détient pas le solde. Le propriétaire est une Party/Org (identity) ou une UO (network).
 */
@Entity
@Table(name = "account_alias", indexes = {
        @Index(name = "idx_account_alias_owner", columnList = "owner_kind, owner_id"),
        @Index(name = "idx_account_alias_tenant", columnList = "tenant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_alias_tenant_code", columnNames = {"tenant_id", "alias_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountAlias {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "alias_code", nullable = false, length = 80)
    private String aliasCode;

    @Column(nullable = false, length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "alias_type", nullable = false, length = 32)
    private AliasType aliasType;

    @Column(name = "external_ref", nullable = false, length = 120)
    private String externalRef;

    @Column(length = 8)
    @Builder.Default
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_kind", nullable = false, length = 16)
    private AliasOwnerKind ownerKind;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "provider_code", length = 50)
    private String providerCode;

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
