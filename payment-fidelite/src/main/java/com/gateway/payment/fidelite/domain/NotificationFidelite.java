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

import java.time.Instant;
import java.util.UUID;

/** Message prêt à partir. Le module ne contacte pas le fournisseur du canal. */
@Entity
@Table(
        name = "notification_fidelite",
        uniqueConstraints = @UniqueConstraint(
                name = "notification_fidelite_dedup_uk",
                columnNames = {"tenant_id", "dedup_key", "canal"})
)
@Getter
@Setter
@NoArgsConstructor
public class NotificationFidelite {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(name = "programme_id")
    private UUID programmeId;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(nullable = false, length = 400)
    private String message;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "dedup_key", nullable = false, length = 180)
    private String dedupKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
