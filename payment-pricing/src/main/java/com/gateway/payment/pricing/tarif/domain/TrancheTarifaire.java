package com.gateway.payment.pricing.tarif.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Intervalle de montants, bornes incluses ; {@code montantMax} vide = tranche ouverte. */
@Entity
@Table(name = "tranche_tarifaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrancheTarifaire {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grille_id", nullable = false)
    private GrilleTarifaire grille;

    @Column(nullable = false)
    private int ordre;

    @Column(name = "montant_min", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantMin;

    @Column(name = "montant_max", precision = 18, scale = 2)
    private BigDecimal montantMax;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private Formule formule;

    /** Valeurs saisies pour les paramètres de la formule (nombres en BigDecimal). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @Builder.Default
    private Map<String, Object> parametres = new LinkedHashMap<>();

    @Column(name = "frais_min", precision = 18, scale = 2)
    private BigDecimal fraisMin;

    @Column(name = "frais_max", precision = 18, scale = 2)
    private BigDecimal fraisMax;

    public boolean couvre(BigDecimal montant) {
        return montant.compareTo(montantMin) >= 0
                && (montantMax == null || montant.compareTo(montantMax) <= 0);
    }
}
