package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Échange d'une récompense : les points sont aussi retirés du solde. */
@Entity
@Table(name = "echange")
@Getter
@Setter
@NoArgsConstructor
public class Echange {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recompense_id", nullable = false)
    private Recompense recompense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "business_reference", length = 150)
    private String businessReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
