package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
        name = "contribution",
        uniqueConstraints = @UniqueConstraint(
                name = "contribution_ref_uk",
                columnNames = {"tenant_id", "business_reference"})
)
@Getter
@Setter
@NoArgsConstructor
public class Contribution {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "programme_id", nullable = false)
    private UUID programmeId;

    @Column(name = "marchand_id")
    private UUID marchandId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 20)
    private String sens;

    @Column(nullable = false)
    private long points;

    @Column(name = "business_reference", nullable = false, length = 150)
    private String businessReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
