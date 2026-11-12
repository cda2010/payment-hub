package com.m2i.pti.service.dto;


import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the FournisseurService entity.
 */
public class FournisseurServiceDTO implements Serializable {

    private Long id;

    @NotNull
    private String codeFse;

    @NotNull
    private String libelleFse;

    private Long paysId;

    private String paysLibellePays;

    private String codeExterne;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getLibelleFse() {
        return libelleFse;
    }

    public void setLibelleFse(String libelleFse) {
        this.libelleFse = libelleFse;
    }

    public Long getPaysId() {
        return paysId;
    }

    public void setPaysId(Long paysId) {
        this.paysId = paysId;
    }

    public String getPaysLibellePays() {
        return paysLibellePays;
    }

    public void setPaysLibellePays(String paysLibellePays) {
        this.paysLibellePays = paysLibellePays;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FournisseurServiceDTO fournisseurServiceDTO = (FournisseurServiceDTO) o;
        if(fournisseurServiceDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), fournisseurServiceDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FournisseurServiceDTO{" +
            "id=" + getId() +
            ", codeFse='" + getCodeFse() + "'" +
            ", libelleFse='" + getLibelleFse() + "'" +
            "}";
    }
}
