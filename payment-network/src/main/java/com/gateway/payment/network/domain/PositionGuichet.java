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
import java.util.UUID;

/** Situation du point de vente : espèces attendues et solde du compte d'opération. */
@Entity
@Table(name = "position_guichet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionGuichet {

    @Id
    @Column(name = "point_vente_id")
    private UUID pointVenteId;

    @Column(name = "solde_compte", nullable = false, precision = 18, scale = 2)
    private BigDecimal soldeCompte;

    @Column(name = "fonds_attendu", nullable = false, precision = 18, scale = 2)
    private BigDecimal fondsAttendu;

    @Column(nullable = false, length = 8)
    private String devise;
}
