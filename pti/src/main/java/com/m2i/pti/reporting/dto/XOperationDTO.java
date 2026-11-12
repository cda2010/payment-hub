package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XOperationDTO {

    private String uoEmetteur;
    private String uoAcq;
    private String canal;
    private String motif;
    private BigDecimal montantFrais;
    private String typeServiceTraite;
    private String uoEm;
    private String emetteur;
    private String beneficiaire;

    private String numero;
    private String dateOp;
    private String typeService;
    private String paysOrigine;
    private String nomEnvoyeur;
    private String nomBenefiaire;
    private String paysBeneficiaire;
    private String nomBanqueBeneficiaire;
    private String numeroCompteBeneficiaire;
    private BigDecimal montant;
    private BigDecimal total;


    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setTypeServiceTraite(String typeServiceTraite) {
        this.typeServiceTraite = typeServiceTraite;
    }

    public String getEmetteur() {
        return emetteur;
    }

    public XOperationDTO emetteur(String emetteur) {
        this.emetteur = emetteur;
        return this;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public XOperationDTO beneficiaire(String beneficaire) {
        this.beneficiaire = beneficaire;
        return this;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public String getTypeServiceTraite() {
        return typeServiceTraite;
    }

    public XOperationDTO typeServiceTraite(String typeServiceTraite) {
        this.typeServiceTraite = typeServiceTraite;
        return this;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public XOperationDTO montantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
        return this;
    }

    public void setUoEm(String uoEm) {
        this.uoEm = uoEm;
    }

    public String getUoEm() {
        return uoEm;
    }

    public XOperationDTO uoEm(String uoEm) {
        this.uoEm = uoEm;
        return this;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getTypeService() {
        return typeService;
    }

    public XOperationDTO typeService(String typeService) {
        this.typeService = typeService;
        return this;
    }

    public void setDateOp(String dateOp) {
        this.dateOp = dateOp;
    }

    public String getUoAcq() {
        return uoAcq;
    }

    public void setUoAcq(String uoAcq) {
        this.uoAcq = uoAcq;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDateOp() {
        return dateOp;
    }

    public void setEmetteur(String emetteur) {
        this.emetteur = emetteur;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getPaysOrigine() {
        return paysOrigine;
    }

    public void setPaysOrigine(String paysOrigine) {
        this.paysOrigine = paysOrigine;
    }

    public String getNomEnvoyeur() {
        return nomEnvoyeur;
    }

    public void setNomEnvoyeur(String nomEnvoyeur) {
        this.nomEnvoyeur = nomEnvoyeur;
    }

    public String getNomBenefiaire() {
        return nomBenefiaire;
    }

    public void setNomBenefiaire(String nomBenefiaire) {
        this.nomBenefiaire = nomBenefiaire;
    }

    public String getPaysBeneficiaire() {
        return paysBeneficiaire;
    }

    public void setPaysBeneficiaire(String paysBeneficiaire) {
        this.paysBeneficiaire = paysBeneficiaire;
    }

    public String getNomBanqueBeneficiaire() {
        return nomBanqueBeneficiaire;
    }

    public void setNomBanqueBeneficiaire(String nomBanqueBeneficiaire) {
        this.nomBanqueBeneficiaire = nomBanqueBeneficiaire;
    }

    public String getNumeroCompteBeneficiaire() {
        return numeroCompteBeneficiaire;
    }

    public void setNumeroCompteBeneficiaire(String numeroCompteBeneficiaire) {
        this.numeroCompteBeneficiaire = numeroCompteBeneficiaire;
    }

    public XOperationDTO dateOp(String dateOp) {
        this.dateOp = dateOp;
        return this;
    }

    public XOperationDTO uoAcq(String uoAcq) {
        this.uoAcq = uoAcq;
        return this;
    }

    public String getUoEmetteur() {
        return uoEmetteur;
    }

    public void setUoEmetteur(String uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
    }

    public XOperationDTO uoEmetteur(String uoEmetteur) {
        this.uoEmetteur = uoEmetteur;
        return this;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public XOperationDTO canal(String canal) {
        this.canal = canal;
        return this;
    }

    public String getMotif() {
        return motif;
    }

    public XOperationDTO motif(String motif) {
        this.motif = motif;
        return this;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public XOperationDTO montant(BigDecimal montant) {
        this.montant = montant;
        return this;
    }
}
