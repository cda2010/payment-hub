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

/** Récompense du catalogue proposée par un programme. */
@Entity
@Table(
        name = "programme_catalogue_recompense",
        uniqueConstraints = @UniqueConstraint(
                name = "programme_catalogue_recompense_uk",
                columnNames = {"programme_id", "catalogue_recompense_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProgrammeCatalogueRecompense {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalogue_recompense_id", nullable = false)
    private CatalogueRecompense catalogue;

    @Column(nullable = false)
    private int position;

    @Column
    private Integer stock;

    @Column(name = "stock_restant")
    private Integer stockRestant;

    @Column(name = "valide_du")
    private Instant valideDu;

    @Column(name = "valide_au")
    private Instant valideAu;

    @Column(name = "marchand_id")
    private UUID marchandId;

    @Column(name = "taux_cashback", precision = 12, scale = 4)
    private BigDecimal tauxCashback;

    @Column(name = "financeur_id")
    private UUID financeurId;
}
