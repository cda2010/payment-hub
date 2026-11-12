package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the EtiquetteComptable entity.
 */
public class EtiquetteComptableDTO implements Serializable {

    private Long id;

    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EtiquetteComptableDTO etiquetteComptableDTO = (EtiquetteComptableDTO) o;
        if(etiquetteComptableDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), etiquetteComptableDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "EtiquetteComptableDTO{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            "}";
    }
}
