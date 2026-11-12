package com.m2i.pti.reporting.dto;

public class XFacture {
    public String referenceFacature;
    public String montantFacture;
    public String referencePaiement;
    public String datePaiement;
    public String produit;
    public String logo;
    public String fmt;
    public String telephone;
    public String nom;
    public String prenom;
    private String referenceExt;
    private String transactionIdExt;
    private String extraData;


    public String getReferenceFacature() {
        return referenceFacature;
    }

    public void setReferenceFacature(String referenceFacature) {
        this.referenceFacature = referenceFacature;
    }

    public String getMontantFacture() {
        return montantFacture;
    }

    public void setMontantFacture(String montantFacture) {
        this.montantFacture = montantFacture;
    }

    public String getReferencePaiement() {
        return referencePaiement;
    }

    public void setReferencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getReferenceExt() {
        return referenceExt;
    }

    public void setReferenceExt(String referenceExt) {
        this.referenceExt = referenceExt;
    }

    public String getTransactionIdExt() {
        return transactionIdExt;
    }

    public void setTransactionIdExt(String transactionIdExt) {
        this.transactionIdExt = transactionIdExt;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
