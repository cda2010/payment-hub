package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parrainage")
@Getter
@Setter
@NoArgsConstructor
public class Parrainage {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "parrain_party_id", nullable = false)
    private UUID parrainPartyId;

    @Column(name = "filleul_party_id")
    private UUID filleulPartyId;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ParrainageStatut statut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "kyc_approved", nullable = false)
    private boolean kycApproved;

    @Column(name = "eligible_event_id")
    private UUID eligibleEventId;

    @Column(name = "eligible_at")
    private Instant eligibleAt;

    @Column(name = "qualified_at")
    private Instant qualifiedAt;

    @Column(name = "rejection_reason", length = 40)
    private String rejectionReason;
}
