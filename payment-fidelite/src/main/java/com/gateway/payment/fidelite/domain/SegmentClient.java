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
        name = "segment_client",
        uniqueConstraints = @UniqueConstraint(
                name = "segment_client_uk",
                columnNames = {"tenant_id", "party_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
public class SegmentClient {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "calcule_at", nullable = false)
    private Instant calculeAt;
}
