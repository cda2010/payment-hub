package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Grille tarifaire de référence pour un produit dans un profil.
 * {@code grilleCode} = code payment-pricing (stable entre versions) ; canal vide = tous canaux.
 */
@Entity
@Table(name = "identity_profile_grid", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_profile_grid", columnNames = {"profile_id", "produit_code", "canal_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityProfileGrid {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "produit_code", nullable = false, length = 80)
    private String produitCode;

    @Column(name = "grille_code", nullable = false, length = 64)
    private String grilleCode;

    /** Vide = tous les canaux. */
    @Column(name = "canal_code", nullable = false, length = 40)
    @Builder.Default
    private String canalCode = "";

    @Column(length = 3)
    private String devise;
}
