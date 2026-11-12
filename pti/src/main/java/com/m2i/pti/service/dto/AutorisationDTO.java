package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Autorisation entity.
 */
public class AutorisationDTO implements Serializable {

    private Long id;

    private String code;

    private Long uniteOrganisationId;

    private String uniteOrganisationLibelleUniteOrganisation;

    private Long typeServiceId;

    private Long clientId;

    private String typeServiceLibelle;

    public AutorisationDTO() {
    }

    public AutorisationDTO(Long id, String code, Long uniteOrganisationId, Long typeServiceId, Long clientId) {
        this.id = id;
        this.code = code;
        this.uniteOrganisationId = uniteOrganisationId;
        this.typeServiceId = typeServiceId;
        this.clientId = clientId;
    }

    public AutorisationDTO(Long id, String code, Long uniteOrganisationId, String uniteOrganisationLibelleUniteOrganisation, Long typeServiceId, Long clientId, String typeServiceLibelle) {
        this.id = id;
        this.code = code;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
        this.typeServiceId = typeServiceId;
        this.clientId = clientId;
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

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

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public String getUniteOrganisationLibelleUniteOrganisation() {
        return uniteOrganisationLibelleUniteOrganisation;
    }

    public void setUniteOrganisationLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AutorisationDTO autorisationDTO = (AutorisationDTO) o;
        if(autorisationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), autorisationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AutorisationDTO{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            "}";
    }
}
