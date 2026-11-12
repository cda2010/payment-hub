package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Service / produit du catalogue autorisé par un profil (référence par code, ex. {@code BILL_PAYMENT}). */
@Entity
@Table(name = "identity_profile_service", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_profile_service", columnNames = {"profile_id", "service_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityProfileService {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "service_code", nullable = false, length = 80)
    private String serviceCode;

    @Column(length = 200)
    private String libelle;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
