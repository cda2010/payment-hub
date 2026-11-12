package com.m2i.pti.service.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A DTO for the PointVente entity.
 */
public class PointVenteDTO implements Serializable {

    private Long id;

    private String codePointVente;

    private String libellePointVente;

    private Long uniteOrganisationId;

    private String uniteOrganisationLibelleUniteOrganisation;

    private String latitude;

    private String longitude;

    private Boolean estActif;
    private Boolean estSurCard;
    private List<Long> userIds;
    Long typeUoId;
    private String libelleUniteOrganisation;
    private String codeUo;
    private String typeUOCode;
    private String niveauTypeUo;

    public PointVenteDTO() {
    }

    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId, Long typeUoId, String libelleUniteOrganisation, String codeUo, String typeUOCode, String niveauTypeUo) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
        this.typeUoId = typeUoId;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
        this.codeUo = codeUo;
        this.typeUOCode = typeUOCode;
        this.niveauTypeUo = niveauTypeUo;
    }

    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId, String uniteOrganisationLibelleUniteOrganisation, String latitude, String longitude, Boolean estActif,Boolean estSurCard, List<Long> userIds) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
        this.uniteOrganisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.estActif = estActif;
          this.estSurCard = estSurCard;
        
        this.userIds = userIds;
    }

    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
    }
    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId, String latitude, String longitude) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId,Boolean estActif,Boolean estSurCard) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
        this.estActif=estActif;
        this.estSurCard=estSurCard;
    }
    public PointVenteDTO(Long id, String codePointVente, String libellePointVente, Long uniteOrganisationId,String longitude,String latitude,Boolean estActif,Boolean estSurCard) {
        this.id = id;
        this.codePointVente = codePointVente;
        this.libellePointVente = libellePointVente;
        this.uniteOrganisationId = uniteOrganisationId;
        this.longitude=longitude;
        this.latitude=latitude;
        this.estActif=estActif;
        this.estSurCard=estSurCard;
    }


    public Boolean getEstSurCard() {
        return estSurCard;
    }

    public void setEstSurCard(Boolean estSurCard) {
        this.estSurCard = estSurCard;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePointVente() {
        return codePointVente;
    }

    public void setCodePointVente(String codePointVente) {
        this.codePointVente = codePointVente;
    }

    public String getLibellePointVente() {
        return libellePointVente;
    }

    public void setLibellePointVente(String libellePointVente) {
        this.libellePointVente = libellePointVente;
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getEstActif() {
        return estActif;
    }

    public void setEstActif(Boolean estActif) {
        this.estActif = estActif;
    }

    public Long getTypeUoId() {
        return typeUoId;
    }

    public void setTypeUoId(Long typeUoId) {
        this.typeUoId = typeUoId;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeUOCode() {
        return typeUOCode;
    }

    public void setTypeUOCode(String typeUOCode) {
        this.typeUOCode = typeUOCode;
    }

    public String getNiveauTypeUo() {
        return niveauTypeUo;
    }

    public void setNiveauTypeUo(String niveauTypeUo) {
        this.niveauTypeUo = niveauTypeUo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PointVenteDTO pointVenteDTO = (PointVenteDTO) o;
        if (pointVenteDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pointVenteDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PointVenteDTO{"
                + "id=" + getId()
                + ", codePointVente='" + getCodePointVente() + "'"
                + ", libellePointVente='" + getLibellePointVente() + "'"
                + "}";
    }
}
