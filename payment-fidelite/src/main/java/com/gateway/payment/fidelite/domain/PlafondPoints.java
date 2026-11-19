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

import java.util.UUID;

@Entity
@Table(
        name = "plafond_points",
        uniqueConstraints = @UniqueConstraint(
                name = "plafond_points_uk",
                columnNames = {"tenant_id", "programme_id", "party_id", "portee", "periode"})
)
@Getter
@Setter
@NoArgsConstructor
public class PlafondPoints {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "programme_id", nullable = false)
    private UUID programmeId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 20)
    private String portee;

    @Column(nullable = false, length = 40)
    private String periode;

    @Column(nullable = false)
    private long points;
}
