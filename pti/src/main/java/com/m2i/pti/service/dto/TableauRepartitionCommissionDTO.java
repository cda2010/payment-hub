package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the TableauRepartitionCommission entity.
 */
public class TableauRepartitionCommissionDTO implements Serializable {

    private Long id;

    private Integer niveaoOperation;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Long typeServiceId;

    private String typeServiceLibelle;

    private Long organisationId;

    private String organisationLibelle;

    private Boolean activated;
    private Long groupId;
    private  String groupLibelle;

    public TableauRepartitionCommissionDTO() {
    }

    public TableauRepartitionCommissionDTO(Long id, Integer niveaoOperation, LocalDate dateDebut, LocalDate dateFin, Long typeServiceId, String typeServiceLibelle, Long organisationId, String organisationLibelle, Boolean activated, Long groupId, String groupLibelle) {
        this.id = id;
        this.niveaoOperation = niveaoOperation;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.typeServiceId = typeServiceId;
        this.typeServiceLibelle = typeServiceLibelle;
        this.organisationId = organisationId;
        this.organisationLibelle = organisationLibelle;
        this.activated = activated;
        this.groupId = groupId;
        this.groupLibelle = groupLibelle;
    }


    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationLibelle() {
        return organisationLibelle;
    }

    public void setOrganisationLibelle(String organisationLibelle) {
        this.organisationLibelle = organisationLibelle;
    }

    public Integer getNiveaoOperation() {
        return niveaoOperation;
    }

    public void setNiveaoOperation(Integer niveaoOperation) {
        this.niveaoOperation = niveaoOperation;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupLibelle() {
        return groupLibelle;
    }

    public void setGroupLibelle(String groupLibelle) {
        this.groupLibelle = groupLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO = (TableauRepartitionCommissionDTO) o;
        if(tableauRepartitionCommissionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauRepartitionCommissionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauRepartitionCommissionDTO{" +
            "id=" + getId() +
            ", niveaoOperation=" + getNiveaoOperation() +
            ", dateDebut='" + getDateDebut() + "'" +
            ", dateFin='" + getDateFin() + "'" +
            "}";
    }
}
