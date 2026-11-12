package com.gateway.payment.network.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Appartenance d'une unité à un regroupement ; le regroupement couvre aussi toute la descendance du membre. */
@Entity
@Table(name = "regroupement_membre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegroupementMembre {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "regroupement_id", nullable = false, updatable = false)
    private UUID regroupementId;

    @Column(name = "unite_id", nullable = false, updatable = false)
    private UUID uniteId;

    @Column(name = "ajoute_le", nullable = false, updatable = false)
    private Instant ajouteLe;

    @Column(name = "ajoute_par", nullable = false, length = 100, updatable = false)
    private String ajoutePar;

    @PrePersist
    void onCreate() {
        ajouteLe = Instant.now();
    }
}
