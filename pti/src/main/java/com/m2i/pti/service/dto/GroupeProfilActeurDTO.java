package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.DestinationProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A DTO for the GroupeProfilActeur entity.
 */
public class GroupeProfilActeurDTO implements Serializable {

    private Long id;

    private String nom;

    private String description;

    private TypeProfilActeur type;

    private String codeFSE;

    private Boolean active;

    public List<ProfilActeurDTO> profils;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeProfilActeur getType() {
        return type;
    }

    public void setType(TypeProfilActeur type) {
        this.type = type;
    }

    public String getCodeFSE() {
        return codeFSE;
    }

    public void setCodeFSE(String codeFSE) {
        this.codeFSE = codeFSE;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<ProfilActeurDTO> getProfils() {
        return profils;
    }

    public void setProfils(List<ProfilActeurDTO> profils) {
        this.profils = profils;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupeProfilActeurDTO groupeProfilActeurDTO = (GroupeProfilActeurDTO) o;
        if(groupeProfilActeurDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), groupeProfilActeurDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GroupeProfilActeurDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", type='" + getType() + "'" +
            ", codeFSE='" + getCodeFSE() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }
}
