package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Affectation d'un profil d'offre à une unité d'organisation (réseau). */
@Entity
@Table(name = "identity_unite_profile", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_unite_profile", columnNames = {"unite_id", "profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityUniteProfile {

    @Id
    @UuidGenerator
    private UUID id;

    /** Id de l'unité dans payment-network (référence logique). */
    @Column(name = "unite_id", nullable = false)
    private UUID uniteId;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (dateDebut == null) {
            dateDebut = LocalDate.now();
        }
    }

    public boolean couvre(LocalDate date) {
        if (!active) {
            return false;
        }
        if (dateDebut != null && date.isBefore(dateDebut)) {
            return false;
        }
        return dateFin == null || !date.isAfter(dateFin);
    }
}
