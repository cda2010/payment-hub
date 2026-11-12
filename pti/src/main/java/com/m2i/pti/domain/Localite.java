package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Localite.
 */
@Entity
@Table(name = "localite")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Localite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_region")
    private String nomRegion;

    @Column(name = "nom_province")
    private String nomProvince;

    @Column(name = "nom_commune")
    private String nomCommune;

    @Column(name = "nom_ville")
    private String nomVille;

    @Column(name = "secteur")
    private String secteur;

    @Column(name = "quartier")
    private String quartier;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomRegion() {
        return nomRegion;
    }

    public Localite nomRegion(String nomRegion) {
        this.nomRegion = nomRegion;
        return this;
    }

    public void setNomRegion(String nomRegion) {
        this.nomRegion = nomRegion;
    }

    public String getNomProvince() {
        return nomProvince;
    }

    public Localite nomProvince(String nomProvince) {
        this.nomProvince = nomProvince;
        return this;
    }

    public void setNomProvince(String nomProvince) {
        this.nomProvince = nomProvince;
    }

    public String getNomCommune() {
        return nomCommune;
    }

    public Localite nomCommune(String nomCommune) {
        this.nomCommune = nomCommune;
        return this;
    }

    public void setNomCommune(String nomCommune) {
        this.nomCommune = nomCommune;
    }

    public String getNomVille() {
        return nomVille;
    }

    public Localite nomVille(String nomVille) {
        this.nomVille = nomVille;
        return this;
    }

    public void setNomVille(String nomVille) {
        this.nomVille = nomVille;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
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
        Localite localite = (Localite) o;
        if (localite.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), localite.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Localite{" +
            "id=" + getId() +
            ", nomRegion='" + getNomRegion() + "'" +
            ", nomProvince='" + getNomProvince() + "'" +
            ", nomCommune='" + getNomCommune() + "'" +
            ", nomVille='" + getNomVille() + "'" +
            "}";
    }
}
