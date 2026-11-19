package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "consentement",
        uniqueConstraints = @UniqueConstraint(name = "consentement_party_uk", columnNames = {"tenant_id", "party_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Consentement {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false)
    private boolean push;

    @Column(nullable = false)
    private boolean sms;

    @Column(nullable = false)
    private boolean email;

    @Column(nullable = false)
    private boolean whatsapp;
}
