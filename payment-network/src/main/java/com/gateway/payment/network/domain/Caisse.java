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

/** Séance d'un caissier. Le compte d'opération est l'alias étiqueté CPTEOPERATION. */
@Entity
@Table(name = "caisse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {

    @Id
    private UUID id;

    @Column(name = "point_vente_id", nullable = false)
    private UUID pointVenteId;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 16)
    private String statut;

    @Column(name = "fonds_attendu", nullable = false, precision = 18, scale = 2)
    private BigDecimal fondsAttendu;

    @Column(name = "fonds_declare", nullable = false, precision = 18, scale = 2)
    private BigDecimal fondsDeclare;

    @Column(name = "ecart_ouverture", nullable = false, precision = 18, scale = 2)
    private BigDecimal ecartOuverture;

    @Column(name = "solde_compte_ouverture", nullable = false, precision = 18, scale = 2)
    private BigDecimal soldeCompteOuverture;

    @Column(name = "fonds_physique_cloture", precision = 18, scale = 2)
    private BigDecimal fondsPhysiqueCloture;

    @Column(name = "solde_compte_fermeture", precision = 18, scale = 2)
    private BigDecimal soldeCompteFermeture;

    @Column(name = "ecart_fermeture", precision = 18, scale = 2)
    private BigDecimal ecartFermeture;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal depots;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal retraits;

    @Column(length = 200)
    private String motif;

    @Column(name = "ouverte_le", nullable = false)
    private Instant ouverteLe;

    @Column(name = "fermee_le")
    private Instant fermeeLe;

    @Column(name = "ouverte_par", nullable = false, length = 100)
    private String ouvertePar;

    @Column(name = "fermee_par", length = 100)
    private String fermeePar;

    @Column(name = "regularisee_le")
    private Instant regulariseeLe;

    @Column(name = "regularisee_par", length = 100)
    private String regulariseePar;

    public boolean estOuverte() {
        return "OUVERTE".equals(statut);
    }
}
