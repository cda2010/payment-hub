package com.m2i.pti.service.dto;


import java.time.LocalDate;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the BilanOperation entity.
 */
public class BilanOperationDTO implements Serializable {

    private Long id;

    private Integer nombre;

    private BigDecimal montant;

    private LocalDate dateDerniereOperation;

    private Long typeServiceId;

    private String typeServiceLibelle;

    private Long canalId;

    private String canalLibelleCanal;

    private Long beneficiaireId;

    private String beneficiaireCodeClient;

    private Long clientEmetteurId;

    private Long clientBeneficiaireId;


    private Long emetteurId;

    private String emetteurCodeClient;

    private Long organisationEmId;

    private String organisationEmLibelleUniteOrganisation;

    private Long organisationAcqId;

    private String organisationAcqLibelleUniteOrganisation;

    private Long acteurId;

    private String acteurLibelleUniteOrganisation;


    public BilanOperationDTO() {
    }

    public BilanOperationDTO(Long id, LocalDate dateDerniereOperation , BigDecimal montant, Integer nombre, String typeServiceLibelle) {
        this.id = id;
        this.dateDerniereOperation = dateDerniereOperation;
        this.montant = montant;
        this.nombre = nombre;
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientEmetteurId() {
        return clientEmetteurId;
    }

    public void setClientEmetteurId(Long clientEmetteurId) {
        this.clientEmetteurId = clientEmetteurId;
    }

    public Long getClientBeneficiaireId() {
        return clientBeneficiaireId;
    }

    public void setClientBeneficiaireId(Long clientBeneficiaireId) {
        this.clientBeneficiaireId = clientBeneficiaireId;
    }

    public Integer getNombre() {
        return nombre;
    }

    public void setNombre(Integer nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateDerniereOperation() {
        return dateDerniereOperation;
    }

    public void setDateDerniereOperation(LocalDate dateDerniereOperation) {
        this.dateDerniereOperation = dateDerniereOperation;
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

    public String getCanalLibelleCanal() {
        return canalLibelleCanal;
    }

    public void setCanalLibelleCanal(String canalLibelleCanal) {
        this.canalLibelleCanal = canalLibelleCanal;
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

    public Long getActeurId() {
        return acteurId;
    }

    public void setActeurId(Long uniteOrganisationId) {
        this.acteurId = uniteOrganisationId;
    }

    public String getActeurLibelleUniteOrganisation() {
        return acteurLibelleUniteOrganisation;
    }

    public void setActeurLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.acteurLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BilanOperationDTO bilanOperationDTO = (BilanOperationDTO) o;
        if(bilanOperationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bilanOperationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BilanOperationDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", montant='" + getMontant() + "'" +
            ", dateDerniereOperation='" + getDateDerniereOperation() + "'" +
            "}";
    }
}
