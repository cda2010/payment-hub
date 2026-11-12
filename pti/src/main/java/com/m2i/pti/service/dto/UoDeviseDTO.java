package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the UoDevise entity.
 */
public class UoDeviseDTO implements Serializable {

    private Long id;

    private Boolean defaultDevise;
   private String code;
    private Long deviseId;

    private String deviseLibelle;

    private Long uniteOrganisationId;

    private String libelleUniteOrganisation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isDefaultDevise() {
        return defaultDevise;
    }

    public Boolean getDefaultDevise() {
        return defaultDevise;
    }

    public void setDefaultDevise(Boolean defaultDevise) {
        this.defaultDevise = defaultDevise;
    }

    public Long getDeviseId() {
        return deviseId;
    }

    public void setDeviseId(Long deviseId) {
        this.deviseId = deviseId;
    }

    public String getDeviseLibelle() {
        return deviseLibelle;
    }

    public void setDeviseLibelle(String deviseLibelle) {
        this.deviseLibelle = deviseLibelle;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
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

        UoDeviseDTO uoDeviseDTO = (UoDeviseDTO) o;
        if(uoDeviseDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), uoDeviseDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UoDeviseDTO{" +
            "id=" + getId() +
            ", defaultDevise='" + isDefaultDevise() + "'" +
            "}";
    }
}
