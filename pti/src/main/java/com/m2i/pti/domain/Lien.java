package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Lien.
 */
@Entity
@Table(name = "lien")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Lien implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lien_potentiel")
    private Long lienPotentiel;

    @Column(name = "valider")
    private Boolean valider;

    @ManyToOne
    private Personne personne;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLienPotentiel() {
        return lienPotentiel;
    }

    public Lien lienPotentiel(Long lienPotentiel) {
        this.lienPotentiel = lienPotentiel;
        return this;
    }

    public void setLienPotentiel(Long lienPotentiel) {
        this.lienPotentiel = lienPotentiel;
    }

    public Boolean isValider() {
        return valider;
    }

    public Lien valider(Boolean valider) {
        this.valider = valider;
        return this;
    }

    public void setValider(Boolean valider) {
        this.valider = valider;
    }

    public Personne getPersonne() {
        return personne;
    }

    public Lien personne(Personne personne) {
        this.personne = personne;
        return this;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
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
        Lien lien = (Lien) o;
        if (lien.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), lien.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Lien{" +
            "id=" + getId() +
            ", lienPotentiel='" + getLienPotentiel() + "'" +
            ", valider='" + isValider() + "'" +
                ",personneId"+getPersonne().getId()+
            "}";
    }
}
