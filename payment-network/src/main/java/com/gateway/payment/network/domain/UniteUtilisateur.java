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
import java.util.UUID;

/** Rattachement d'une personne à une unité. Le même login peut avoir une ligne par unité. */
@Entity
@Table(name = "unite_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniteUtilisateur {

    @Id
    private UUID id;

    @Column(name = "unite_id", nullable = false)
    private UUID uniteId;

    /** Login personnel. Ce n'est pas le numéro de téléphone. */
    @Column(nullable = false, length = 80)
    private String identifiant;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(length = 20)
    private String telephone;

    @Column(length = 180)
    private String email;

    /** SMS ou EMAIL : canal qui reçoit le mot de passe temporaire. Vide pour les comptes déjà ouverts. */
    @Column(length = 8)
    private String canal;

    @Column(nullable = false, length = 10)
    private String statut;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean estActif() {
        return "ACTIF".equals(statut);
    }
}
