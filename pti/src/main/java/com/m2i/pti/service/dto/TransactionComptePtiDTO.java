package com.m2i.pti.service.dto;


import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the TransactionComptePti entity.
 */
public class TransactionComptePtiDTO implements Serializable {

    private Long id;

    @NotNull
    private TypeTransactionCompte type;

    @NotNull
    private EtatValidite etat;

    @NotNull
    private ZonedDateTime date;

    @NotNull
    private BigDecimal montant;

    private BigDecimal soldeCourant;

    private BigDecimal soldeApres;

    private Long operationId;

    private String operationStatut;

    private String codeOperation;

    private String operationMotif;

    private Long compteAutreId;

    private String valeurCompte;

    private String etiquette;

    private Instant createdDate;

    private String createdBy;

    private String operationCompteSrc;

    private String typeService;

    private String codeExterne;

    private String OrganisationLibelle;

    private String clientTelephone;

    private String libelleCompte;
    private String description;

    public TransactionComptePtiDTO() {
    }

    public TransactionComptePtiDTO(Long id, String type, String etat,Long compteAutreId, BigDecimal soldeCourant, BigDecimal soldeApres,BigDecimal montant, Long operationId,String createdBy) {
        this.id = id;
        this.type = type!=null? TypeTransactionCompte.valueOf(type):null;
        this.etat = etat!=null? EtatValidite.valueOf(etat):null;
        this.compteAutreId = compteAutreId;
        this.soldeCourant = soldeCourant;
        this.soldeApres = soldeApres;
        this.montant = montant;
        this.operationId = operationId;
        this.createdBy = createdBy;
    }

    public TransactionComptePtiDTO(Long id, String type, String etat, ZonedDateTime date,
                                   BigDecimal montant, BigDecimal soldeCourant, BigDecimal soldeApres,
                                   Long operationId, String operationStatut, String codeOperation, String operationMotif,
                                   Long compteAutreId, String createdBy,String typeService, String operationCompteSrc, String codeExterne) {
        this.id = id;
        this.type = type!=null?TypeTransactionCompte.valueOf(type):null;
        this.etat = etat!=null?EtatValidite.valueOf(etat):null;
        this.date = date;
        this.montant = montant;
        this.soldeCourant = soldeCourant;
        this.soldeApres = soldeApres;
        this.operationId = operationId;
        this.operationStatut = operationStatut;
        this.codeOperation = codeOperation;
        this.operationMotif = operationMotif;
        this.compteAutreId = compteAutreId;
        this.createdBy = createdBy;
        this.typeService = typeService;
        this.operationCompteSrc = operationCompteSrc;
        this.codeExterne = codeExterne;
    }

    public TransactionComptePtiDTO(Long id, String type, String etat, ZonedDateTime date,
                                   BigDecimal montant, BigDecimal soldeCourant, BigDecimal soldeApres,
                                   Long operationId, String operationStatut, String codeOperation, String operationMotif,
                                   Long compteAutreId, String createdBy,String typeService, String operationCompteSrc, String codeExterne,String description) {
        this.id = id;
        this.type = type!=null?TypeTransactionCompte.valueOf(type):null;
        this.etat = etat!=null?EtatValidite.valueOf(etat):null;
        this.date = date;
        this.montant = montant;
        this.soldeCourant = soldeCourant;
        this.soldeApres = soldeApres;
        this.operationId = operationId;
        this.operationStatut = operationStatut;
        this.codeOperation = codeOperation;
        this.operationMotif = operationMotif;
        this.compteAutreId = compteAutreId;
        this.createdBy = createdBy;
        this.typeService = typeService;
        this.operationCompteSrc = operationCompteSrc;
        this.codeExterne = codeExterne;
        this.description=description;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeTransactionCompte getType() {
        return type;
    }

    public void setType(TypeTransactionCompte type) {
        this.type = type;
    }

    public EtatValidite getEtat() {
        return etat;
    }

    public void setEtat(EtatValidite etat) {
        this.etat = etat;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public Long getCompteAutreId() {
        return compteAutreId;
    }

    public void setCompteAutreId(Long compteAutreId) {
        this.compteAutreId = compteAutreId;
    }

    public String getValeurCompte() {
        return valeurCompte;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getSoldeCourant() {
        return soldeCourant;
    }

    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }

    public BigDecimal getSoldeApres() {
        return soldeApres;
    }

    public void setSoldeApres(BigDecimal soldeApres) {
        this.soldeApres = soldeApres;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOperationStatut() {
        return operationStatut;
    }

    public void setOperationStatut(String operationStatut) {
        this.operationStatut = operationStatut;
    }

    public String getOperationMotif() {
        return operationMotif;
    }

    public void setOperationMotif(String operationMotif) {
        this.operationMotif = operationMotif;
    }

    public String getOperationCompteSrc() {
        return operationCompteSrc;
    }

    public void setOperationCompteSrc(String operationCompteSrc) {
        this.operationCompteSrc = operationCompteSrc;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public String getOrganisationLibelle() {
        return OrganisationLibelle;
    }

    public void setOrganisationLibelle(String organisationLibelle) {
        OrganisationLibelle = organisationLibelle;
    }

    public String getClientTelephone() {
        return clientTelephone;
    }

    public void setClientTelephone(String clientTelephone) {
        this.clientTelephone = clientTelephone;
    }

    public String getLibelleCompte() {
        return libelleCompte;
    }

    public void setLibelleCompte(String libelleCompte) {
        this.libelleCompte = libelleCompte;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransactionComptePtiDTO transactionComptePtiDTO = (TransactionComptePtiDTO) o;
        if(transactionComptePtiDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), transactionComptePtiDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }


    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", etat='" + getEtat() + "'" +
            ", date='" + getDate() + "'" +
            ", montant='" + getMontant() + "'" +
            ", soldeCourant='" + getSoldeCourant() + "'" +
            ", soldeApres='" + getSoldeApres() + "'" +
            ", operationId='" + getOperationId() + "'" +
            ", operationStatut='" + getOperationStatut() + "'" +
            ", codeOperation='" + getCodeOperation() + "'" +
            ", operationMotif='" + getOperationMotif() + "'" +
            ", compteAutreId='" + getCompteAutreId() + "'" +
            ", valeurCompte='" + getValeurCompte() + "'" +
            ", etiquette='" + getEtiquette() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", operationCompteSrc='" + getOperationCompteSrc() + "'" +
            ", typeService='" + getTypeService() + "'" +
            ", codeExterne='" + getCodeExterne() + "'" +
            ", OrganisationLibelle='" + getOrganisationLibelle() + "'" +
            ", clientTelephone='" + getClientTelephone() + "'" +
            ", libelleCompte='" + getLibelleCompte() + "'" +
            "}";
    }

}
