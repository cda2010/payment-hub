package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the TypeCompte entity.
 */
public class TypeCompteDTO implements Serializable {

    private Long id;

    @NotNull
    private String codeTypeCompte;

    @NotNull
    private String libelleTypeCompte;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeTypeCompte() {
        return codeTypeCompte;
    }

    public void setCodeTypeCompte(String codeTypeCompte) {
        this.codeTypeCompte = codeTypeCompte;
    }

    public String getLibelleTypeCompte() {
        return libelleTypeCompte;
    }

    public void setLibelleTypeCompte(String libelleTypeCompte) {
        this.libelleTypeCompte = libelleTypeCompte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeCompteDTO typeCompteDTO = (TypeCompteDTO) o;
        if(typeCompteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeCompteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeCompteDTO{" +
            "id=" + getId() +
            ", codeTypeCompte=" + getCodeTypeCompte() +
            ", libelleTypeCompte='" + getLibelleTypeCompte() + "'" +
            "}";
    }
}
