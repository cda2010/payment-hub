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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Avantage monétaire enregistré. Le wallet n'est pas crédité ici. */
@Entity
@Table(
        name = "avantage_monetaire",
        uniqueConstraints = @UniqueConstraint(
                name = "avantage_monetaire_ref_uk",
                columnNames = {"tenant_id", "business_reference"})
)
@Getter
@Setter
@NoArgsConstructor
public class AvantageMonetaire {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "programme_id", nullable = false)
    private UUID programmeId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false)
    private long points;

    @Column(name = "valeur_xof", nullable = false, precision = 19, scale = 2)
    private BigDecimal valeurXof;

    @Column(name = "business_reference", nullable = false, length = 150)
    private String businessReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
