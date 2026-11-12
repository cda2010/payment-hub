package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A StructureOrganisationFacturier.
 */
@Entity
@Table(name = "structure_organisation_facturier")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StructureOrganisationFacturier extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "niveau")
    private Integer niveau;

    @Column(name = "niveausup")
    private Integer niveausup;

    @Column(name = "libelle")
    private String libelle;

    @ManyToOne
    // @JsonIgnoreProperties("uniteOrganisation")
    private UniteOrganisation uniteOrganisation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public StructureOrganisationFacturier niveau(Integer niveau) {
        this.niveau = niveau;
        return this;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public Integer getNiveausup() {
        return niveausup;
    }

    public StructureOrganisationFacturier niveausup(Integer niveausup) {
        this.niveausup = niveausup;
        return this;
    }

    public void setNiveausup(Integer niveausup) {
        this.niveausup = niveausup;
    }

    public String getLibelle() {
        return libelle;
    }

    public StructureOrganisationFacturier libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }
    public StructureOrganisationFacturier uniteOrganisation(UniteOrganisation uniteOrganisation) {
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
        StructureOrganisationFacturier structureOrganisationFacturier = (StructureOrganisationFacturier) o;
        if (structureOrganisationFacturier.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), structureOrganisationFacturier.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "StructureOrganisationFacturier{" +
            "id=" + getId() +
            ", niveau='" + getNiveau() + "'" +
            ", niveausup='" + getNiveausup() + "'" +
            ", libelle='" + getLibelle() +"}";
    }
}
