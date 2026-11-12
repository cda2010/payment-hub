package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A EtiquetteCompte.
 */
@Entity
@Table(name = "etiquette_compte")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EtiquetteCompte extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "obligatoire")
    private Boolean obligatoire;

    @Column(name = "transversal")
    private Boolean transversal;

    @Column(name = "niveau")
    private Integer niveau;


 @Column(name = "libelle")
    private String libelle;

    @Column(name = "estDebiteur")
    private Boolean estDebiteur;
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public EtiquetteCompte code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean isObligatoire() {
        return obligatoire;
    }

    public EtiquetteCompte obligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
        return this;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }

    public Boolean isTransversal() {
        return transversal;
    }

    public void setTransversal(Boolean transversal) {
        this.transversal = transversal;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Boolean getEstDebiteur() {
        return estDebiteur;
    }

    public void setEstDebiteur(Boolean estDebiteur) {
        this.estDebiteur = estDebiteur;
    }



    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EtiquetteCompte etiquetteCompte = (EtiquetteCompte) o;
        if (etiquetteCompte.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), etiquetteCompte.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "EtiquetteCompte{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
                 ", getLibelle='" + getLibelle() + "'" +
            ", obligatoire='" + isObligatoire() + "'" +
            "}";
    }
}
