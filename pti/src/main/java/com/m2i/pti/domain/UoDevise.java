package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A UoDevise.
 */
@Entity
@Table(name = "uo_devise")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UoDevise extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_devise")
    private Boolean defaultDevise;

    @ManyToOne
    private Devise devise;

    @ManyToOne
    private UniteOrganisation uniteOrganisation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isDefaultDevise() {
        return defaultDevise;
    }

    public UoDevise defaultDevise(Boolean defaultDevise) {
        this.defaultDevise = defaultDevise;
        return this;
    }

    public void setDefaultDevise(Boolean defaultDevise) {
        this.defaultDevise = defaultDevise;
    }

    public Boolean getDefaultDevise() {
        return defaultDevise;
    }

    public Devise getDevise() {
        return devise;
    }

    public UoDevise devise(Devise devise) {
        this.devise = devise;
        return this;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public UoDevise uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
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
        UoDevise uoDevise = (UoDevise) o;
        if (uoDevise.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), uoDevise.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UoDevise{" +
            "id=" + getId() +
            ", defaultDevise='" + isDefaultDevise() + "'" +
            "}";
    }
}
