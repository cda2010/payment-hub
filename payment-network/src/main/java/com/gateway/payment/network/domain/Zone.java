package com.gateway.payment.network.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Zone monétaire (UEMOA, CEMAC… et celles ajoutées par l'administration). Une branche entière du réseau reste dans
 * la même zone ; les unités la référencent par son code, qui ne change pas.
 */
@Entity
@Table(name = "zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    /** Code ISO 4217 ; figé dès que la zone contient une unité. */
    @Column(nullable = false, length = 3)
    private String devise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutUnite statut;

    /** Null tant que la zone n'est pas enregistrée : Spring Data fait alors un persist, pas un merge. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean estActive() {
        return statut == StatutUnite.ACTIF;
    }
}
