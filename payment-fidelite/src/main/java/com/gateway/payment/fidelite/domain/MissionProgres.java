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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "mission_progres",
        uniqueConstraints = @UniqueConstraint(
                name = "mission_progres_uk",
                columnNames = {"mission_id", "party_id", "fenetre_debut"})
)
@Getter
@Setter
@NoArgsConstructor
public class MissionProgres {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "fenetre_debut", nullable = false)
    private LocalDate fenetreDebut;

    @Column(nullable = false)
    private int compteur;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

    @Column(length = 400)
    private String services;

    @Column(nullable = false)
    private boolean accomplie;
}
