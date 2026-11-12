package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

public class TransactionComptePtiOryx implements Serializable {

    private Long id;

    @NotNull
    private String type;

    @NotNull
    private String etat;

    @NotNull
    private ZonedDateTime date_operation;

    @NotNull
    private BigDecimal montant;

    private BigDecimal soldeCourant;

    private BigDecimal soldeApres;

    private Long operationId;

    private String operationStatut;

    private String codeOperation;

    private String operationMotif;

    private Long compteAutreId;

    private String createdBy;
    private String typeService;
    private String operationCompteSrc;

    private String codeExterne;

    private String soldeApresDernierJour;

    private String dateDernierJour;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public ZonedDateTime getDate_operation() {
        return date_operation;
    }

    public void setDate_operation(ZonedDateTime date_operation) {
        this.date_operation = date_operation;
    }

    public BigDecimal getMontant() {
        return montant;
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

    public String getOperationStatut() {
        return operationStatut;
    }

    public void setOperationStatut(String operationStatut) {
        this.operationStatut = operationStatut;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getOperationMotif() {
        return operationMotif;
    }

    public void setOperationMotif(String operationMotif) {
        this.operationMotif = operationMotif;
    }

    public Long getCompteAutreId() {
        return compteAutreId;
    }

    public void setCompteAutreId(Long compteAutreId) {
        this.compteAutreId = compteAutreId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getOperationCompteSrc() {
        return operationCompteSrc;
    }

    public void setOperationCompteSrc(String operationCompteSrc) {
        this.operationCompteSrc = operationCompteSrc;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

    public String getSoldeApresDernierJour() {
        return soldeApresDernierJour;
    }

    public void setSoldeApresDernierJour(String soldeApresDernierJour) {
        this.soldeApresDernierJour = soldeApresDernierJour;
    }

    public String getDateDernierJour() {
        return dateDernierJour;
    }

    public void setDateDernierJour(String dateDernierJour) {
        this.dateDernierJour = dateDernierJour;
    }

    public TransactionComptePtiOryx(Long id, String type, String etat, ZonedDateTime date_operation, BigDecimal montant, BigDecimal soldeCourant, BigDecimal soldeApres, Long operationId, String operationStatut, String codeOperation, String operationMotif, Long compteAutreId, String createdBy, String typeService, String operationCompteSrc, String codeExterne, String soldeApresDernierJour, String dateDernierJour) {
        this.id = id;
        this.type = type;
        this.etat = etat;
        this.date_operation = date_operation;
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
        this.soldeApresDernierJour = soldeApresDernierJour;
        this.dateDernierJour = dateDernierJour;
    }

    public TransactionComptePtiOryx() {
    }
}