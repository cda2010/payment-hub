package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XBeneficiaire {
    public String dateOp;
    public BigDecimal montantOp;
    public String numOp;
    public String typeService;
    public String numCompteBeneficiaire;
    public String nomPrenom;

    public String getDateOp() {
        return dateOp;
    }

    public void setDateOp(String dateOp) {
        this.dateOp = dateOp;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public String getNumOp() {
        return numOp;
    }

    public void setNumOp(String numOp) {
        this.numOp = numOp;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getNumCompteBeneficiaire() {
        return numCompteBeneficiaire;
    }

    public void setNumCompteBeneficiaire(String numCompteBeneficiaire) {
        this.numCompteBeneficiaire = numCompteBeneficiaire;
    }

    public String getNomPrenom() {
        return nomPrenom;
    }

    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }
}
