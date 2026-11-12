package com.m2i.pti.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A BilanTaxe.
 */
@Entity
@Table(name = "bilan_taxe")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BilanTaxe extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant")
    private BigDecimal montant;

    @Column(name = "est_paye")
    private Boolean estPaye;

    @ManyToOne
    @JsonIgnoreProperties("")
    private Operation operation;

    @ManyToOne
    private UniteOrganisation organisation;

    @ManyToOne
    @JsonIgnoreProperties("")
    private TaxeOperation taxeOperation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public BilanTaxe montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Boolean getEstPaye() {
        return estPaye;
    }

    public void setEstPaye(Boolean estPaye) {
        this.estPaye = estPaye;
    }

    public UniteOrganisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(UniteOrganisation organisation) {
        this.organisation = organisation;
    }

    public Operation getOperation() {
        return operation;
    }

    public BilanTaxe operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public TaxeOperation getTaxeOperation() {
        return taxeOperation;
    }

    public BilanTaxe taxeOperation(TaxeOperation taxeOperation) {
        this.taxeOperation = taxeOperation;
        return this;
    }

    public void setTaxeOperation(TaxeOperation taxeOperation) {
        this.taxeOperation = taxeOperation;
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
        BilanTaxe bilanTaxe = (BilanTaxe) o;
        if (bilanTaxe.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bilanTaxe.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BilanTaxe{" +
                "id=" + getId() +
                ", montant=" + getMontant() +
                "}";
    }
}
