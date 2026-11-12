package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ClassificationFacturier.
 */
@Entity
@Table(name = "classification_facturier")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable(value = true)
public class ClassificationFacturier extends AbstractAuditingEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle_classification")
    private String libelleClassification;

    @ManyToOne
    private UniteOrganisation uniteOrganisation ;

    @ManyToOne
    private ClassificationFacturier niveauSuperieur ;
  @Column
    private boolean activated = true;
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleClassification() {
        return libelleClassification;
    }

    public ClassificationFacturier libelleClassification(String libelleClassification) {
        this.libelleClassification = libelleClassification;
        return this;
    }

    public void setLibelleClassification(String libelleClassification) {
        this.libelleClassification = libelleClassification;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public ClassificationFacturier uniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
        return this;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public ClassificationFacturier getNiveauSuperieur() {
        return niveauSuperieur;
    }

    public ClassificationFacturier niveauSuperieur(ClassificationFacturier niveauSuperieur) {
        this.niveauSuperieur= niveauSuperieur;
        return this;
    }

    public void setNiveauSuperieur(ClassificationFacturier niveauSuperieur) {
        this.niveauSuperieur = niveauSuperieur;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassificationFacturier classificationFacturier = (ClassificationFacturier) o;
        if (classificationFacturier.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), classificationFacturier.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
