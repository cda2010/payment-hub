package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "point_movement",
        uniqueConstraints = @UniqueConstraint(
                name = "point_movement_reference_uk",
                columnNames = {"tenant_id", "business_reference"})
)
@Getter
@Setter
@NoArgsConstructor
public class PointMovement {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount account;

    @Column(name = "business_reference", nullable = false, length = 150)
    private String businessReference;

    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType;

    @Column(nullable = false)
    private long points;

    @Column(nullable = false, length = 160)
    private String motif;

    @Column(name = "rule_version_id")
    private UUID ruleVersionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
