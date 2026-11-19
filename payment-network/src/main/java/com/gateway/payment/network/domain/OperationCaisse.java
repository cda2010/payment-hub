package com.gateway.payment.network.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Dépôt ou retrait rattaché à une séance de caisse ouverte. */
@Entity
@Table(name = "operation_caisse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationCaisse {

    @Id
    private UUID id;

    @Column(name = "caisse_id", nullable = false)
    private UUID caisseId;

    @Column(nullable = false, length = 10)
    private String sens;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montant;

    @Column(length = 80)
    private String reference;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "cree_par", nullable = false, length = 100)
    private String creePar;
}
