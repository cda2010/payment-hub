package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeTarif;

/**
 * A LigneFrais.
 */
@Entity
@Table(name = "ligne_frais")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LigneFrais extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_frais")
    private TypeTarif typeFrais;

    @Column(name = "maximun")
    private Double maximun;

    @Column(name = "minimun")
    private Double minimun;

    @Column(name = "valeur")
    private Double valeur;

    @ManyToOne
    private TableauFrais tableauFrais;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTarif getTypeFrais() {
        return typeFrais;
    }

    public LigneFrais typeFrais(TypeTarif typeFrais) {
        this.typeFrais = typeFrais;
        return this;
    }

    public void setTypeFrais(TypeTarif typeFrais) {
        this.typeFrais = typeFrais;
    }

    public Double getMaximun() {
        return maximun;
    }

    public void setMaximun(Double maximun) {
        this.maximun = maximun;
    }

    public Double getMinimun() {
        return minimun;
    }

    public void setMinimun(Double minimun) {
        this.minimun = minimun;
    }

    public Double getValeur() {
        return valeur;
    }

    public LigneFrais valeur(Double valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public TableauFrais getTableauFrais() {
        return tableauFrais;
    }

    public LigneFrais tableauFrais(TableauFrais tableauFrais) {
        this.tableauFrais = tableauFrais;
        return this;
    }

    public void setTableauFrais(TableauFrais tableauFrais) {
        this.tableauFrais = tableauFrais;
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
        LigneFrais ligneFrais = (LigneFrais) o;
        if (ligneFrais.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), ligneFrais.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LigneFrais{" +
            "id=" + getId() +
            ", typeFrais='" + getTypeFrais() + "'" +
            ", maximun=" + getMaximun() +
            ", minimun=" + getMinimun() +
            ", valeur=" + getValeur() +
            "}";
    }
}
