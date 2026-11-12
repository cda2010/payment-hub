package com.pti.emeraude.gateway.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MOperationPartenaire implements Serializable {
    private Long id;
    private Double montantOp;
    private String codeOperation;
    private ZonedDateTime dateOperation;
    private String statut;
    private String codeExterne;
    private String beneficiaire;
    private String typeService;
    private String telephone;

    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Double getMontantOp() {
        return montantOp;
    }
    public void setMontantOp(Double montantOp) {
        this.montantOp = montantOp;
    }
    public String getCodeOperation() {
        return codeOperation;
    }
    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }
    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }
    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }
    public String getStatut() {
        return statut;
    }
    public void setStatut(String statut) {
        this.statut = statut;
    }
    public String getCodeExterne() {
        return codeExterne;
    }
    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }
    public String getBeneficiaire() {
        return beneficiaire;
    }
    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }
    public String getTypeService() {
        return typeService;
    }
    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
