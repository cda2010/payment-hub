package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Canal entity.
 */
public class CanalDTO implements Serializable {

    private Long id;
  
    private String codeCanal;

   
    private String libelleCanal;

    private Set<TypeServiceDTO> typeServices = new HashSet<>();

    public CanalDTO() {
    }

    public CanalDTO(Long id, String codeCanal, String libelleCanal) {
        this.id = id;
        this.codeCanal = codeCanal;
        this.libelleCanal = libelleCanal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeCanal() {
        return codeCanal;
    }

    public void setCodeCanal(String codeCanal) {
        this.codeCanal = codeCanal;
    }

    public String getLibelleCanal() {
        return libelleCanal;
    }

    public void setLibelleCanal(String libelleCanal) {
        this.libelleCanal = libelleCanal;
    }

    public Set<TypeServiceDTO> getTypeServices() {
        return typeServices;
    }

    public void setTypeServices(Set<TypeServiceDTO> typeServices) {
        this.typeServices = typeServices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CanalDTO canalDTO = (CanalDTO) o;
        if(canalDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), canalDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CanalDTO{" +
            "id=" + getId() +
            ", codeCanal='" + getCodeCanal() + "'" +
            ", libelleCanal='" + getLibelleCanal() + "'" +
            "}";
    }
}
