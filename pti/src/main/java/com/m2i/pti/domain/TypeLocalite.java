package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A TypeLocalite.
 */
@Entity
@Table(name = "type_localite")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeLocalite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "niveau")
    private Integer niveau;

    @ManyToOne
    private UniteOrganisation pays;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public TypeLocalite libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public TypeLocalite niveau(Integer niveau) {
        this.niveau = niveau;
        return this;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public UniteOrganisation getPays() {
        return pays;
    }

    public TypeLocalite pays(UniteOrganisation uniteOrganisation) {
        this.pays = uniteOrganisation;
        return this;
    }

    public void setPays(UniteOrganisation uniteOrganisation) {
        this.pays = uniteOrganisation;
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
        TypeLocalite typeLocalite = (TypeLocalite) o;
        if (typeLocalite.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeLocalite.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeLocalite{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            ", niveau='" + getNiveau() + "'" +
            "}";
    }
}
