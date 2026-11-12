package com.gateway.payment.fidelite.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Niveau d'un programme : Bronze, Argent, Or, Platine, ou un palier propre au programme. */
@Entity
@Table(
        name = "palier",
        uniqueConstraints = {
                @UniqueConstraint(name = "palier_programme_code_uk", columnNames = {"programme_id", "code"}),
                @UniqueConstraint(name = "palier_programme_seuil_uk", columnNames = {"programme_id", "seuil_points"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Palier {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(name = "seuil_points", nullable = false)
    private int seuilPoints;

    /** Points gagnés pour 1 000 F de paiement une fois ce palier atteint. */
    @Column(name = "points_par_mille", nullable = false, precision = 8, scale = 2)
    private BigDecimal pointsParMille;

    @ElementCollection
    @CollectionTable(name = "palier_avantage", joinColumns = @JoinColumn(name = "palier_id"))
    @OrderColumn(name = "position")
    @Column(name = "avantage", nullable = false, length = 160)
    private List<String> avantages = new ArrayList<>();
}
