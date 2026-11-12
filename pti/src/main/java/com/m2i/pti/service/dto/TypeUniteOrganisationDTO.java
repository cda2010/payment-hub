package com.m2i.pti.service.dto;


import com.m2i.pti.domain.Authority;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the TypeUniteOrganisation entity.
 */
public class TypeUniteOrganisationDTO implements Serializable {

    private Long id;

    private String libelleTypeUO;

    private String codeTypeUO;

    private Integer niveau;

    private Set<String> authorities;

    public TypeUniteOrganisationDTO(Long id, String codeTypeUO,String libelleTypeUO, Integer niveau) {
        this.id = id;
        this.libelleTypeUO = libelleTypeUO;
        this.codeTypeUO = codeTypeUO;
        this.niveau = niveau;
    }

    public TypeUniteOrganisationDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleTypeUO() {
        return libelleTypeUO;
    }

    public void setLibelleTypeUO(String libelleTypeUO) {
        this.libelleTypeUO = libelleTypeUO;
    }

    public String getCodeTypeUO() {
        return codeTypeUO;
    }

    public void setCodeTypeUO(String codeTypeUO) {
        this.codeTypeUO = codeTypeUO;
    }

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeUniteOrganisationDTO typeUniteOrganisationDTO = (TypeUniteOrganisationDTO) o;
        if(typeUniteOrganisationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeUniteOrganisationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeUniteOrganisationDTO{" +
            "id=" + getId() +
            ", libelleTypeUO='" + getLibelleTypeUO() + "'" +
            ", codeTypeUO='" + getCodeTypeUO() + "'" +
            ", niveau=" + getNiveau() +
            "}";
    }
}
