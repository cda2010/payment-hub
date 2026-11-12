package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the TableauFrais entity.
 */
public class TableauFraisDTO implements Serializable {

    private Long id;

    private Integer poidsNum;

    private Long typeServiceId;

    private String typeServiceLibelle;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Long canalId;

    private String canalLibelleCanal;

    private Long typeUoEmetteurId;

    private String typeUoEmetteurLibelleTypeUO;

    private Long typeUoAcquereurId;

    private String typeUoAcquereurLibelleTypeUO;

    private Long beneficiaireId;

    private String beneficiaireCodeClient;

    private Long emetteurId;

    private String emetteurCodeClient;

    private Long organisationEmId;

    private String organisationEmLibelleUniteOrganisation;

    private Long organisationAcqId;

    private String organisationAcqLibelleUniteOrganisation;

    private Boolean activated;

    private String libelle;
  private String typeRegle;
    private String sourceCalcul;

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

    public Integer getPoidsNum() {
        return poidsNum;
    }

    public void setPoidsNum(Integer poidsNum) {
        this.poidsNum = poidsNum;
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

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
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

    public String getCanalLibelleCanal() {
        return canalLibelleCanal;
    }

    public void setCanalLibelleCanal(String canalLibelleCanal) {
        this.canalLibelleCanal = canalLibelleCanal;
    }

    public Long getTypeUoEmetteurId() {
        return typeUoEmetteurId;
    }

    public void setTypeUoEmetteurId(Long typeUniteOrganisationId) {
        this.typeUoEmetteurId = typeUniteOrganisationId;
    }

    public String getTypeUoEmetteurLibelleTypeUO() {
        return typeUoEmetteurLibelleTypeUO;
    }

    public void setTypeUoEmetteurLibelleTypeUO(String typeUniteOrganisationLibelleTypeUO) {
        this.typeUoEmetteurLibelleTypeUO = typeUniteOrganisationLibelleTypeUO;
    }

    public Long getTypeUoAcquereurId() {
        return typeUoAcquereurId;
    }

    public void setTypeUoAcquereurId(Long typeUniteOrganisationId) {
        this.typeUoAcquereurId = typeUniteOrganisationId;
    }

    public String getTypeUoAcquereurLibelleTypeUO() {
        return typeUoAcquereurLibelleTypeUO;
    }

    public void setTypeUoAcquereurLibelleTypeUO(String typeUniteOrganisationLibelleTypeUO) {
        this.typeUoAcquereurLibelleTypeUO = typeUniteOrganisationLibelleTypeUO;
    }

    public Long getBeneficiaireId() {
        return beneficiaireId;
    }

    public void setBeneficiaireId(Long clientId) {
        this.beneficiaireId = clientId;
    }

    public String getBeneficiaireCodeClient() {
        return beneficiaireCodeClient;
    }

    public void setBeneficiaireCodeClient(String clientCodeClient) {
        this.beneficiaireCodeClient = clientCodeClient;
    }

    public Long getEmetteurId() {
        return emetteurId;
    }

    public void setEmetteurId(Long clientId) {
        this.emetteurId = clientId;
    }

    public String getEmetteurCodeClient() {
        return emetteurCodeClient;
    }

    public void setEmetteurCodeClient(String clientCodeClient) {
        this.emetteurCodeClient = clientCodeClient;
    }

    public Long getOrganisationEmId() {
        return organisationEmId;
    }

    public void setOrganisationEmId(Long uniteOrganisationId) {
        this.organisationEmId = uniteOrganisationId;
    }

    public String getOrganisationEmLibelleUniteOrganisation() {
        return organisationEmLibelleUniteOrganisation;
    }

    public void setOrganisationEmLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.organisationEmLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public Long getOrganisationAcqId() {
        return organisationAcqId;
    }

    public void setOrganisationAcqId(Long uniteOrganisationId) {
        this.organisationAcqId = uniteOrganisationId;
    }

    public String getOrganisationAcqLibelleUniteOrganisation() {
        return organisationAcqLibelleUniteOrganisation;
    }

    public void setOrganisationAcqLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.organisationAcqLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getTypeRegle() {
        return typeRegle;
    }

    public void setTypeRegle(String typeRegle) {
        this.typeRegle = typeRegle;
    }

    public String getSourceCalcul() {
        return sourceCalcul;
    }

    public void setSourceCalcul(String sourceCalcul) {
        this.sourceCalcul = sourceCalcul;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableauFraisDTO tableauFraisDTO = (TableauFraisDTO) o;
        if (tableauFraisDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tableauFraisDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TableauFraisDTO{" +
                "id=" + getId() +
                ", poidsNum=" + getPoidsNum() +
                "}";
    }
}
