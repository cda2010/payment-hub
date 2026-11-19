package com.gateway.payment.network.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Période pendant laquelle un caissier tient un point de vente. */
@Entity
@Table(name = "affectation_caissier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectationCaissier {

    @Id
    private UUID id;

    @Column(name = "point_vente_id", nullable = false)
    private UUID pointVenteId;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false)
    private LocalDate debut;

    private LocalDate fin;

    @Column(length = 200)
    private String motif;

    @Column(nullable = false, length = 10)
    private String statut;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private Instant creeLe;

    @Column(name = "cree_par", nullable = false, length = 100, updatable = false)
    private String creePar;

    @Column(name = "cloturee_le")
    private Instant clotureeLe;

    @Column(name = "cloturee_par", length = 100)
    private String clotureePar;

    @PrePersist
    void onCreate() {
        if (creeLe == null) {
            creeLe = Instant.now();
        }
    }

    public boolean estActive() {
        return "ACTIF".equals(statut);
    }
}
