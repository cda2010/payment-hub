package com.m2i.pti.service.dto;

import java.math.BigDecimal;

public class XOperationDTO {

    private String canal;
    private String motif;
    private BigDecimal montantFrais;
    private String uoEm;
    private String emetteur;
    private String beneficiaire;

    private String numero;
    private String dateOp;
    private String typeService;
    private String paysOrigine;
    private String paysBeneficiaire;
    private String nomBanqueBeneficiaire;
    private String numeroCompteBeneficiaire;
    private BigDecimal montant;
    private String profilEmetteur;
    private String profilBeneficiaire;
    private String telephoneEmet;
    private String telephoneBenef;
    private String codeExterne;





    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public String getProfilEmetteur() {
        return profilEmetteur;
    }

    public void setProfilEmetteur(String profilEmetteur) {
        this.profilEmetteur = profilEmetteur;
    }

    public String getProfilBeneficiaire() {
        return profilBeneficiaire;
    }

    public void setProfilBeneficiaire(String profilBeneficiaire) {
        this.profilBeneficiaire = profilBeneficiaire;
    }

    public String getTelephoneEmet() {
        return telephoneEmet;
    }

    public void setTelephoneEmet(String telephoneEmet) {
        this.telephoneEmet = telephoneEmet;
    }

    public String getTelephoneBenef() {
        return telephoneBenef;
    }

    public void setTelephoneBenef(String telephoneBenef) {
        this.telephoneBenef = telephoneBenef;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }
}
