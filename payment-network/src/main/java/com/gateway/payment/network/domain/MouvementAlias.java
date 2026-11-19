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

/** Transfert entre deux alias : vers l'étiquette CPTEOPERATION, ou dans l'autre sens. */
@Entity
@Table(name = "mouvement_alias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementAlias {

    @Id
    private UUID id;

    @Column(name = "unite_id", nullable = false)
    private UUID uniteId;

    @Column(name = "point_vente_id", nullable = false)
    private UUID pointVenteId;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false, length = 20)
    private String sens;

    @Column(name = "alias_source_id", nullable = false)
    private UUID aliasSourceId;

    @Column(name = "alias_cible_id", nullable = false)
    private UUID aliasCibleId;

    @Column(name = "etiquette_source", nullable = false, length = 40)
    private String etiquetteSource;

    @Column(name = "etiquette_cible", nullable = false, length = 40)
    private String etiquetteCible;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 8)
    private String devise;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "cree_par", nullable = false, length = 100)
    private String creePar;
}
