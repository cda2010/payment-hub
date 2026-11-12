package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.util.Objects;
import com.m2i.pti.domain.enumeration.TypeParametre;

/**
 * A DTO for the ParametreApp entity.
 */
public class ParametreAppDTO implements Serializable {

    private Long id;

    private String nom;

    private String valeur;

    private TypeParametre type;

    private String codeFse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public TypeParametre getType() {
        return type;
    }

    public void setType(TypeParametre type) {
        this.type = type;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParametreAppDTO parametreAppDTO = (ParametreAppDTO) o;
        if (parametreAppDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), parametreAppDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ParametreAppDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", valeur='" + getValeur() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
