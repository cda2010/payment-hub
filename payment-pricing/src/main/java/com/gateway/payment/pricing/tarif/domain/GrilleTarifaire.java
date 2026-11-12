package com.gateway.payment.pricing.tarif.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UuidGenerator;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Version d'une grille : périmètre (produit, canal, devise, source, destination, période), règle d'arrondi et
 * tranches. Un canal, une source ou une destination vide signifie « tous » ; le moteur retient la grille la plus
 * précise.
 */
@Entity
@Table(name = "grille_tarifaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleTarifaire {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, length = 80)
    private String produit;

    @Column(length = 40)
    private String canal;

    @Column(nullable = false, length = 3)
    private String devise;

    /** Vide : toutes les sources. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zone", column = @Column(name = "source_zone", length = 10)),
            @AttributeOverride(name = "uniteId", column = @Column(name = "source_unite_id")),
            @AttributeOverride(name = "uniteCode", column = @Column(name = "source_unite_code", length = 40)),
            @AttributeOverride(name = "uniteLibelle", column = @Column(name = "source_unite_libelle", length = 150)),
            @AttributeOverride(name = "uniteType", column = @Column(name = "source_unite_type", length = 20)),
            @AttributeOverride(name = "uniteParentId", column = @Column(name = "source_unite_parent_id"))
    })
    private CibleReseau source;

    /** Vide : toutes les destinations, y compris une opération sans destination. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zone", column = @Column(name = "destination_zone", length = 10)),
            @AttributeOverride(name = "uniteId", column = @Column(name = "destination_unite_id")),
            @AttributeOverride(name = "uniteCode", column = @Column(name = "destination_unite_code", length = 40)),
            @AttributeOverride(name = "uniteLibelle", column = @Column(name = "destination_unite_libelle", length = 150)),
            @AttributeOverride(name = "uniteType", column = @Column(name = "destination_unite_type", length = 20)),
            @AttributeOverride(name = "uniteParentId", column = @Column(name = "destination_unite_parent_id"))
    })
    private CibleReseau destination;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatutGrille statut;

    /** Si vrai, la validation refuse tout trou entre deux tranches. */
    @Column(name = "continuite_requise", nullable = false)
    private boolean continuiteRequise;

    @Column(name = "arrondi_echelle", nullable = false)
    private int arrondiEchelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrondi_mode", nullable = false, length = 16)
    private RoundingMode arrondiMode;

    /** Assiette d'entrée des frais (montant, FX, cumul…). Défaut : montant transaction. */
    @Enumerated(EnumType.STRING)
    @Column(name = "base_calcul", nullable = false, length = 32)
    @Builder.Default
    private BaseCalculFrais baseCalcul = BaseCalculFrais.TRANSACTION_AMOUNT;

    /** Requis si {@link #baseCalcul} est un cumul. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cumul_period", length = 32)
    private PeriodeCumul cumulPeriod;

    /** Requis si {@link #baseCalcul} est un cumul. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cumul_scope", length = 32)
    private ScopeCumul cumulScope;

    @OneToMany(mappedBy = "grille", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @BatchSize(size = 100)
    @Builder.Default
    private List<TrancheTarifaire> tranches = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean estModifiable() {
        return statut == StatutGrille.BROUILLON;
    }

    public String libelleVersion() {
        return code + " v" + version;
    }

    public void ajouterTranche(TrancheTarifaire tranche) {
        tranche.setGrille(this);
        tranches.add(tranche);
    }
}
