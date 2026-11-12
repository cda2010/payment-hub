package com.m2i.pti.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the MotifOperation entity.
 */
public class MotifOperationDTO implements Serializable {

    private Long id;

    public MotifOperationDTO() {
    }

    public MotifOperationDTO(Long id, String code, String libelle) {
        this.id = id;
          this.code = code;
        this.libelle = libelle;
      
    }


    private String libelle;

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MotifOperationDTO motifOperationDTO = (MotifOperationDTO) o;
        if (motifOperationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), motifOperationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MotifOperationDTO{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            "}";
    }
}
