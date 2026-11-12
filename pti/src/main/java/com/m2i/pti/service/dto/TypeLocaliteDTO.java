package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the TypeLocalite entity.
 */
public class TypeLocaliteDTO implements Serializable {

    private Long id;

    private String libelle;

    private Integer niveau;

    private Long paysId;

    private String libellePays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public Long getPaysId() {
        return paysId;
    }

    public void setPaysId(Long uniteOrganisationId) {
        this.paysId = uniteOrganisationId;
    }

    public String getLibellePays() {
        return libellePays;
    }

    public void setLibellePays(String libellePays) {
        this.libellePays = libellePays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeLocaliteDTO typeLocaliteDTO = (TypeLocaliteDTO) o;
        if(typeLocaliteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeLocaliteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeLocaliteDTO{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            ", niveau='" + getNiveau() + "'" +
            "}";
    }
}
