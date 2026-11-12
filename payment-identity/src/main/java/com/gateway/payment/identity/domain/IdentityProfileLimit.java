package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Groupe de règles de référence pour un produit et un sens, dans un profil de limite.
 * {@code groupeCode} = code payment-rules (stable) ; identity ne porte pas les montants.
 */
@Entity
@Table(name = "identity_profile_limit", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_profile_limit", columnNames = {"profile_id", "produit_code", "sens"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityProfileLimit {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "produit_code", nullable = false, length = 80)
    private String produitCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SensLimite sens;

    @Column(name = "groupe_code", nullable = false, length = 64)
    private String groupeCode;
}
