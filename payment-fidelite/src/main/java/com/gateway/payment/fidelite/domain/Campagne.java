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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "campagne",
        uniqueConstraints = @UniqueConstraint(name = "campagne_code_uk", columnNames = {"tenant_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
public class Campagne {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 160)
    private String nom;

    @Column(nullable = false)
    private Instant debut;

    @Column(nullable = false)
    private Instant fin;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal multiplicateur = BigDecimal.ONE;

    @Column(name = "bonus_fixe", nullable = false)
    private long bonusFixe;

    @Column(name = "budget_points")
    private Long budgetPoints;

    @Column(name = "points_attribues", nullable = false)
    private long pointsAttribues;

    @Column(nullable = false)
    private int priorite;

    @Column(length = 40)
    private String groupe;

    @Column(name = "weekend_seulement", nullable = false)
    private boolean weekendSeulement;

    @Column(length = 400)
    private String profils;

    @Column(length = 400)
    private String regions;

    @Column(name = "unite_id")
    private UUID uniteId;

    @Column(length = 400)
    private String produits;

    @Column(length = 400)
    private String services;

    @Column(length = 200)
    private String canaux;

    @Column(name = "financeur_id")
    private UUID financeurId;

    @Column(name = "segment_requis", length = 40)
    private String segmentRequis;

    @Column(nullable = false)
    private boolean actif = true;
}
