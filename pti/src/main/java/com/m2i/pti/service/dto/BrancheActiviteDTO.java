package com.m2i.pti.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the BrancheActivite entity.
 */
public class BrancheActiviteDTO implements Serializable {

    private Long id;

    @NotNull
    private String libelle;

    private String code;

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

        BrancheActiviteDTO brancheActiviteDTO = (BrancheActiviteDTO) o;
        if (brancheActiviteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), brancheActiviteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BrancheActiviteDTO{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            ", code='" + getCode() + "'" +
            "}";
    }
}
