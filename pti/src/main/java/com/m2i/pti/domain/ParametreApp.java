package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import com.m2i.pti.domain.enumeration.TypeParametre;

/**
 * A ParametreApp.
 */
@Entity
@Table(name = "parametre_app")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ParametreApp extends AbstractAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;
    @Column(name = "valeur")
    private String valeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_type")
    private TypeParametre type;
    @Column(name = "code_fse")
    private String codeFse;
    // jhipster-needle-entity-add-field - Jhipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public ParametreApp nom(String nom) {
        this.nom = nom;
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getValeur() {
        return valeur;
    }

    public ParametreApp valeur(String valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public TypeParametre getType() {
        return type;
    }

    public ParametreApp type(TypeParametre type) {
        this.type = type;
        return this;
    }

    public void setType(TypeParametre type) {
        this.type = type;
    }
    // jhipster-needle-entity-add-getters-setters - Jhipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParametreApp parametreApp = (ParametreApp) o;
        if (parametreApp.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), parametreApp.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ParametreApp{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", valeur='" + getValeur() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }
    
}
