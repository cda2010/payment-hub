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
        name = "adhesion",
        uniqueConstraints = @UniqueConstraint(name = "adhesion_programme_party_uk", columnNames = {"programme_id", "party_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Adhesion {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
