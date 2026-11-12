package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Localite entity.
 */
public class LocaliteDTO implements Serializable {

    private Long id;

    private String nomRegion;

    private String nomProvince;

    private String nomCommune;

    private String nomVille;

    private String secteur;

    private String quartier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomRegion() {
        return nomRegion;
    }

    public void setNomRegion(String nomRegion) {
        this.nomRegion = nomRegion;
    }

    public String getNomProvince() {
        return nomProvince;
    }

    public void setNomProvince(String nomProvince) {
        this.nomProvince = nomProvince;
    }

    public String getNomCommune() {
        return nomCommune;
    }

    public void setNomCommune(String nomCommune) {
        this.nomCommune = nomCommune;
    }

    public String getNomVille() {
        return nomVille;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocaliteDTO localiteDTO = (LocaliteDTO) o;
        if(localiteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), localiteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LocaliteDTO{" +
            "id=" + getId() +
            ", nomRegion='" + getNomRegion() + "'" +
            ", nomProvince='" + getNomProvince() + "'" +
            ", nomCommune='" + getNomCommune() + "'" +
            ", nomVille='" + getNomVille() + "'" +
            "}";
    }
}
