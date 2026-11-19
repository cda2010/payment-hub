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

import java.util.UUID;

/** Taux entre deux programmes. Sans programme cible, points_cible est le montant XOF. */
@Entity
@Table(name = "taux_conversion")
@Getter
@Setter
@NoArgsConstructor
public class TauxConversion {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_programme_id", nullable = false)
    private Programme source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cible_programme_id")
    private Programme cible;

    @Column(name = "points_source", nullable = false)
    private int pointsSource;

    @Column(name = "points_cible", nullable = false)
    private int pointsCible;

    @Column(name = "plafond_mois", nullable = false)
    private long plafondMois;

    @Column(nullable = false)
    private boolean actif = true;
}
