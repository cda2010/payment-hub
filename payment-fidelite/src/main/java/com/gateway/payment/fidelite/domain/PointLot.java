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

/** Lot de points encore consommables. L'échéance nulle ne périt pas. */
@Entity
@Table(
        name = "point_lot",
        uniqueConstraints = @UniqueConstraint(
                name = "point_lot_reference_uk",
                columnNames = {"tenant_id", "business_reference"})
)
@Getter
@Setter
@NoArgsConstructor
public class PointLot {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount account;

    @Column(name = "original_points", nullable = false)
    private long originalPoints;

    @Column(name = "remaining_points", nullable = false)
    private long remainingPoints;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "business_reference", nullable = false, length = 150)
    private String businessReference;
}
