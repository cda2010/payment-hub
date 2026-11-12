package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XTransactionOp {
    public String idTransc;
    public String typeService;
    public BigDecimal montantOp;
    public String envoi;
    public String beneficiaire;
    public String numeroCpt;
    public BigDecimal montant;
    public String dateOperation;
    public Integer numero;

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getIdTransc() {
        return idTransc;
    }

    public void setIdTransc(String idTransc) {
        this.idTransc = idTransc;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public BigDecimal getMontantOp() {
        return montantOp;
    }

    public void setMontantOp(BigDecimal montantOp) {
        this.montantOp = montantOp;
    }

    public String getEnvoi() {
        return envoi;
    }

    public void setEnvoi(String envoi) {
        this.envoi = envoi;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getNumeroCpt() {
        return numeroCpt;
    }

    public void setNumeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(String dateOperation) {
        this.dateOperation = dateOperation;
    }
}
