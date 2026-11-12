package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Reglement.
 */
@Entity
@Table(name = "reglement")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Reglement extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_reglement")
    private ZonedDateTime dateReglement;

    @Column(name = "statut")
    private Boolean statut;
    @Column(name = "en_traitement")
    private Boolean enTraitement;
    
    
    @Column(name = "valider_par")
    private String validerPar;

    @Column(name = "estAnnuler")
    private Boolean estAnnuler;

    @Column(name = "montant_total")
    private Double montantTotal ;// jhipster-needle-entity-add-field - JHipster will add fields here, do not remove


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDateReglement() {
        return dateReglement;
    }

    public Reglement dateReglement(ZonedDateTime dateReglement) {
        this.dateReglement = dateReglement;
        return this;
    }

    public void setDateReglement(ZonedDateTime dateReglement) {
        this.dateReglement = dateReglement;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Boolean getStatut() {
        return statut;
    }

    public void setStatut(Boolean statut) {
        this.statut = statut;
    }

    public Boolean getEstAnnuler() {
        return estAnnuler;
    }

    public void setEstAnnuler(Boolean estAnnuler) {
        this.estAnnuler = estAnnuler;
    }

    public Boolean getEnTraitement() {
        return enTraitement;
    }

    public void setEnTraitement(Boolean enTraitement) {
        this.enTraitement = enTraitement;
    }

    @Override
    public String getCreatedBy() {
        return super.getCreatedBy();
    }

    @Override
    public void setCreatedBy(String createdBy) {
        super.setCreatedBy(createdBy);
    }

    @Override
    public Instant getCreatedDate() {
        return super.getCreatedDate();
    }

    @Override
    public void setCreatedDate(Instant createdDate) {
        super.setCreatedDate(createdDate);
    }

    @Override
    public String getLastModifiedBy() {
        return super.getLastModifiedBy();
    }

    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        super.setLastModifiedBy(lastModifiedBy);
    }

    @Override
    public Instant getLastModifiedDate() {
        return super.getLastModifiedDate();
    }

    @Override
    public void setLastModifiedDate(Instant lastModifiedDate) {
        super.setLastModifiedDate(lastModifiedDate);
    }

    public String getValiderPar() {
        return validerPar;
    }

    public void setValiderPar(String validerPar) {
        this.validerPar = validerPar;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reglement reglement = (Reglement) o;
        if (reglement.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), reglement.getId());
    }

    @Override
    public String toString() {
        return "Reglement{"
                + "id=" + getId()
                + ", dateReglement='" + getDateReglement() + "'"
                + ", validerPar='" + getValiderPar() + "'"
                + "}";
    }
}
