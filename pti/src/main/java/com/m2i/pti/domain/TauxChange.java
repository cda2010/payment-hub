package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeChange;

/**
 * A TauxChange.
 */
@Entity
@Table(name = "taux_change")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class TauxChange extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "date_taux", nullable = false)
    private LocalDate dateTaux;

    @NotNull
    @Column(name = "taux", precision=10, scale=4, nullable = false)
    private BigDecimal taux;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_change")
    private TypeChange typeChange;

    @ManyToOne
    private Devise deviseDepart;

    @ManyToOne
    private Devise deviseArrivee;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateTaux() {
        return dateTaux;
    }

    public TauxChange dateTaux(LocalDate dateTaux) {
        this.dateTaux = dateTaux;
        return this;
    }

    public void setDateTaux(LocalDate dateTaux) {
        this.dateTaux = dateTaux;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public TauxChange taux(BigDecimal taux) {
        this.taux = taux;
        return this;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public TypeChange getTypeChange() {
        return typeChange;
    }

    public TauxChange typeChange(TypeChange typeChange) {
        this.typeChange = typeChange;
        return this;
    }

    public void setTypeChange(TypeChange typeChange) {
        this.typeChange = typeChange;
    }

    public Devise getDeviseDepart() {
        return deviseDepart;
    }

    public TauxChange deviseDepart(Devise devise) {
        this.deviseDepart = devise;
        return this;
    }

    public void setDeviseDepart(Devise devise) {
        this.deviseDepart = devise;
    }

    public Devise getDeviseArrivee() {
        return deviseArrivee;
    }

    public TauxChange deviseArrivee(Devise devise) {
        this.deviseArrivee = devise;
        return this;
    }

    public void setDeviseArrivee(Devise devise) {
        this.deviseArrivee = devise;
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
        TauxChange tauxChange = (TauxChange) o;
        if (tauxChange.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tauxChange.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TauxChange{" +
            "id=" + getId() +
            ", dateTaux='" + getDateTaux() + "'" +
            ", taux='" + getTaux() + "'" +
            ", typeChange='" + getTypeChange() + "'" +
            "}";
    }
}
