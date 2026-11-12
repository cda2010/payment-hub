package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.Acteur;

/**
 * A RepartitionOp.
 */
@Entity
@Table(name = "repartition_op")
@Cacheable(value = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RepartitionOp extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jhi_date")
    private ZonedDateTime date;

    @NotNull
    @Column(name = "montant", precision = 10, scale = 2, nullable = false)
    private BigDecimal montant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "acteur", nullable = false)
    private Acteur acteur;

    @ManyToOne
    private Operation operation;

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

    public RepartitionOp montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public RepartitionOp date(ZonedDateTime date) {
        this.date = date;
        return this;
    }


    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Acteur getActeur() {
        return acteur;
    }

    public RepartitionOp acteur(Acteur acteur) {
        this.acteur = acteur;
        return this;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public Operation getOperation() {
        return operation;
    }

    public RepartitionOp operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
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
        RepartitionOp repartitionOp = (RepartitionOp) o;
        if (repartitionOp.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), repartitionOp.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RepartitionOp{" +
                "id=" + getId() +
                ", montant='" + getMontant() + "'" +
                ", acteur='" + getActeur() + "'" +
                "}";
    }
}
