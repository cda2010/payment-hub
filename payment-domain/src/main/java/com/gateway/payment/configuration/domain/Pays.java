package com.gateway.payment.configuration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Référentiel pays. Le nom est celui attendu par l'omnicanal ({@code pays.nom_pays}). */
@Entity
@Table(name = "pays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pays {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 8)
    private String code;

    @Column(nullable = false, unique = true, length = 80)
    private String nom;

    /** Indicatif téléphonique, avec le préfixe +. Exemple : +226. */
    @Column(length = 8)
    private String indicatif;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String devise = "XOF";

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordre = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
}
