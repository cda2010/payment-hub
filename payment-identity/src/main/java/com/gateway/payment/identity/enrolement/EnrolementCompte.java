package com.gateway.payment.identity.enrolement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Compte déjà connu d'une maison Coris. Le téléphone sert au code, il ne sort pas de l'API. */
@Entity
@Table(name = "enrolement_compte")
@Getter
@Setter
@NoArgsConstructor
public class EnrolementCompte {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 16)
    private String maison;

    @Column(nullable = false, length = 40)
    private String numero;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 30)
    private String telephone;
}
