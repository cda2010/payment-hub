package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2i.pti.domain.enumeration.MontantsAComptabilise;
import com.m2i.pti.domain.enumeration.TypeTarif;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A TaxeOperation.
 */
@Entity
@Table(name = "taxe_operation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class TaxeOperation extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_calcul", nullable = false)
    private TypeTarif modeCalcul;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "applique_sur", nullable = false)
    private MontantsAComptabilise appliqueSur;

    @NotNull
    @Column(name = "valeur", precision = 10, scale = 2, nullable = false)
    private BigDecimal valeur;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column
    private boolean activated = false;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    @JoinTable(name = "taxe_operation_type_service",
            joinColumns = @JoinColumn(name = "taxeOperations_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "type_services_id", referencedColumnName = "id"))
    private Set<TypeService> typeServices = new HashSet<>();

    @OneToMany(mappedBy = "taxeOperation")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    private Set<BilanTaxe> bilanTaxes = new HashSet<>();

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    @ManyToOne
    private UniteOrganisation zone;

    @ManyToOne
    private Taxe taxe;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public Taxe getTaxe() {
        return taxe;
    }

    public void setTaxe(Taxe taxe) {
        this.taxe = taxe;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTarif getModeCalcul() {
        return modeCalcul;
    }

    public TaxeOperation modeCalcul(TypeTarif modeCalcul) {
        this.modeCalcul = modeCalcul;
        return this;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public void setModeCalcul(TypeTarif modeCalcul) {
        this.modeCalcul = modeCalcul;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public MontantsAComptabilise getAppliqueSur() {
        return appliqueSur;
    }

    public TaxeOperation appliqueSur(MontantsAComptabilise appliqueSur) {
        this.appliqueSur = appliqueSur;
        return this;
    }

    public UniteOrganisation getZone() {
        return zone;
    }

    public void setZone(UniteOrganisation zone) {
        this.zone = zone;
    }

    public void setAppliqueSur(MontantsAComptabilise appliqueSur) {
        this.appliqueSur = appliqueSur;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public TaxeOperation valeur(BigDecimal valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public TaxeOperation dateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public TaxeOperation dateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Set<TypeService> getTypeServices() {
        return typeServices;
    }

    public TaxeOperation typeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
        return this;
    }

   

    public void setTypeServices(Set<TypeService> typeServices) {
        this.typeServices = typeServices;
    }

    public Set<BilanTaxe> getBilanTaxes() {
        return bilanTaxes;
    }

    public void setBilanTaxes(Set<BilanTaxe> bilanTaxes) {
        this.bilanTaxes = bilanTaxes;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaxeOperation TaxeOperation = (TaxeOperation) o;
        if (TaxeOperation.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), TaxeOperation.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TaxeOperation{" +
                "id=" + getId() +
                ", modeCalcul='" + getModeCalcul() + "'" +
                ", appliqueSur='" + getAppliqueSur() + "'" +
                ", valeur=" + getValeur() +
                ", dateDebut='" + getDateDebut() + "'" +
                ", dateFin='" + getDateFin() + "'" +
                "}";
    }
}
