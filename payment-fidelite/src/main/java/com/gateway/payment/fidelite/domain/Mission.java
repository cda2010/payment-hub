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
import java.util.UUID;

@Entity
@Table(
        name = "mission",
        uniqueConstraints = @UniqueConstraint(name = "mission_code_uk", columnNames = {"tenant_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
public class Mission {

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

    /** COUNT, AMOUNT, DISTINCT ou PREMIER. */
    @Column(nullable = false, length = 20)
    private String metrique;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cible;

    /** Zéro : une seule fenêtre, toute la durée de la mission. */
    @Column(name = "fenetre_jours", nullable = false)
    private int fenetreJours;

    @Column(name = "filtre_produit", length = 80)
    private String filtreProduit;

    @Column(name = "filtre_service", length = 80)
    private String filtreService;

    @Column(name = "filtre_canal", length = 80)
    private String filtreCanal;

    @Column(nullable = false)
    private long points;

    @Column(nullable = false)
    private boolean actif = true;
}
