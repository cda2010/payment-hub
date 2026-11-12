package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A CategorieClient.
 */
@Entity
@Table(name = "categorie_client")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CategorieClient extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_categorie")
    private String libelleCategorie;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleCategorie() {
        return libelleCategorie;
    }

    public CategorieClient libelleCategorie(String libelleCategorie) {
        this.libelleCategorie = libelleCategorie;
        return this;
    }

    public void setLibelleCategorie(String libelleCategorie) {
        this.libelleCategorie = libelleCategorie;
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
        CategorieClient categorieClient = (CategorieClient) o;
        if (categorieClient.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), categorieClient.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CategorieClient{" +
            "id=" + getId() +
            ", libelleCategorie='" + getLibelleCategorie() + "'" +
            "}";
    }
}
