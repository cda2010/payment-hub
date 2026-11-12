package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.domain.enumeration.EtatValidite;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the Commission entity.
 */
public class CommissionDTO implements Serializable {

    private Long id;

    private BigDecimal montant;

    private Boolean estEmetteur;

    private Boolean estPaye;

    private Boolean estRegle;


    private Boolean estComptabilise;

    private ZonedDateTime datePaiement;

    private ZonedDateTime dateEnregistrement;

    private Long operationId;

    private String operationCodeOperation;

    private Long organisationId;

    private String organisationLibelleUniteOrganisation;

    private Acteur acteur;

    private EtatValidite etat;

    private String typeService;

    private BigDecimal montantOp;
    
    private Long ligneReglementId;
    
    private Boolean enTraitement;

    public Boolean getEstEmetteur() {
        return estEmetteur;
    }

    public CommissionDTO() {
    }

    public CommissionDTO(Long id, BigDecimal montant, Long operationId, Long organisationId,  String etat, Boolean estComptabilise, Boolean estPaye, Boolean estRegle) {
        this.id = id;
        this.montant = montant;
        this.estPaye = estPaye;
        this.estRegle = estRegle;
        this.estComptabilise=estComptabilise;
        this.operationId = operationId;
        this.organisationId = organisationId;
        this.etat = EtatValidite.valueOf(etat);
    }
    public CommissionDTO(Long id, BigDecimal montant, Boolean estEmetteur, Boolean estPaye, Boolean estRegle, Boolean estComptabilise, ZonedDateTime datePaiement, ZonedDateTime dateEnregistrement, Long operationId, String operationCodeOperation, Long organisationId, String organisationLibelleUniteOrganisation, String acteur, String etat, String typeService, BigDecimal montantOp) {
        this.id = id;
        this.montant = montant;
        this.estEmetteur = estEmetteur;
        this.estPaye = estPaye;
        this.estRegle = estRegle;
        this.estComptabilise = estComptabilise;
        this.datePaiement = datePaiement;
        this.dateEnregistrement = dateEnregistrement;
        this.operationId = operationId;
        this.operationCodeOperation = operationCodeOperation;
        this.organisationId = organisationId;
        this.organisationLibelleUniteOrganisation = organisationLibelleUniteOrganisation;
        this.acteur = acteur!=null? Acteur.valueOf(acteur):null;
        this.etat = etat!=null? EtatValidite.valueOf(etat):null ;
        this.typeService = typeService;
        this.montantOp = montantOp;
    }
    public CommissionDTO(Long id, BigDecimal montant, Boolean estEmetteur, Boolean estPaye, Boolean estRegle, Boolean estComptabilise,Boolean enTraitement, ZonedDateTime datePaiement, ZonedDateTime dateEnregistrement, Long operationId, String operationCodeOperation, Long organisationId, String organisationLibelleUniteOrganisation, String acteur, String etat, String typeService, BigDecimal montantOp,Long ligneReglementId) {
        this.id = id;
        this.montant = montant;
        this.estEmetteur = estEmetteur;
        this.estPaye = estPaye;
        this.estRegle = estRegle;
        this.estComptabilise = estComptabilise;
        this.datePaiement = datePaiement;
        this.dateEnregistrement = dateEnregistrement;
        this.operationId = operationId;
        this.operationCodeOperation = operationCodeOperation;
        this.organisationId = organisationId;
        this.organisationLibelleUniteOrganisation = organisationLibelleUniteOrganisation;
        this.acteur = acteur!=null? Acteur.valueOf(acteur):null;
        this.etat = etat!=null? EtatValidite.valueOf(etat):null ;
        this.typeService = typeService;
        this.montantOp = montantOp;
        this.ligneReglementId=ligneReglementId;
        this.enTraitement=enTraitement;
    }




    public void setEstEmetteur(Boolean estEmetteur) {
        this.estEmetteur = estEmetteur;
    }

    public Boolean getEstPaye() {
        return estPaye;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Boolean isEstPaye() {
        return estPaye;
    }

    public void setEstPaye(Boolean estPaye) {
        this.estPaye = estPaye;
    }

    public ZonedDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(ZonedDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public ZonedDateTime getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(ZonedDateTime dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public EtatValidite getEtat() {
        return etat;
    }

    public void setEtat(EtatValidite etat) {
        this.etat = etat;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getOperationCodeOperation() {
        return operationCodeOperation;
    }

    public void setOperationCodeOperation(String operationCodeOperation) {
        this.operationCodeOperation = operationCodeOperation;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long uniteOrganisationId) {
        this.organisationId = uniteOrganisationId;
    }

    public String getOrganisationLibelleUniteOrganisation() {
        return organisationLibelleUniteOrganisation;
    }

    public void setOrganisationLibelleUniteOrganisation(String uniteOrganisationLibelleUniteOrganisation) {
        this.organisationLibelleUniteOrganisation = uniteOrganisationLibelleUniteOrganisation;
    }

    public Boolean getEstComptabilise() {
        return estComptabilise;
    }

    public void setEstComptabilise(Boolean estComptabilise) {
        this.estComptabilise = estComptabilise;
    }


    public Acteur getActeur() {
        return acteur;
    }

    public void setActeur(Acteur acteur) {
        this.acteur = acteur;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public Boolean getEstRegle() {
        return estRegle;
    }

    public void setEstRegle(Boolean estRegle) {
        this.estRegle = estRegle;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public Long getLigneReglementId() {
        return ligneReglementId;
    }

    public void setLigneReglementId(Long ligneReglementId) {
        this.ligneReglementId = ligneReglementId;
    }

    public Boolean getEnTraitement() {
        return enTraitement;
    }

    public void setEnTraitement(Boolean enTraitement) {
        this.enTraitement = enTraitement;
    }
    
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommissionDTO commissionDTO = (CommissionDTO) o;
        if(commissionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), commissionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CommissionDTO{" +
            "id=" + getId() +
            ", montant=" + getMontant() +
            ", estPaye='" + isEstPaye() + "'" +
            ", datePaiement='" + getDatePaiement() + "'" +
            ", dateEnregistrement='" + getDateEnregistrement() + "'" +
            "}";
    }
}
