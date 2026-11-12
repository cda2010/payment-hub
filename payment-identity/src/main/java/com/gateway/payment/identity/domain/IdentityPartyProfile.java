package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Affectation d'un profil d'offre à une Party. */
@Entity
@Table(name = "identity_party_profile", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_party_profile", columnNames = {"party_id", "profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityPartyProfile {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

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

    /** Valide à la date donnée (actif et dans la fenêtre de dates). */
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
