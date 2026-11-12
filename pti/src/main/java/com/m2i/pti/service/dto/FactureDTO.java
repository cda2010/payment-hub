package com.m2i.pti.service.dto;

public class FactureDTO {
   private Long id;
   private  String datePaiement;
   private String montantPaiement;
   private String referenceFacature;
   private String referencePaiement;
   private String referenceExt;
   private String transactionIdExt;
   private String produit;
   private Long  operationId;
   private String telephone;
   private String nom;
   private String prenom;
   private String strExtraData;
   private String extraData;



    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatePaiement() {
        return this.datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getMontantPaiement() {
        return this.montantPaiement;
    }

    public void setMontantPaiement(String montantPaiement) {
        this.montantPaiement = montantPaiement;
    }

    public String getReferenceFacature() {
        return this.referenceFacature;
    }

    public void setReferenceFacature(String referenceFacature) {
        this.referenceFacature = referenceFacature;
    }

    public String getReferencePaiement() {
        return this.referencePaiement;
    }

    public void setReferencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
    }

    public String getProduit() {
        return this.produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getTransactionIdExt() {
        return transactionIdExt;
    }

    public void setTransactionIdExt(String transactionIdExt) {
        this.transactionIdExt = transactionIdExt;
    }

    public String getReferenceExt() {
        return referenceExt;
    }

    public void setReferenceExt(String referenceExt) {
        this.referenceExt = referenceExt;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
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

    public String getStrExtraData() {
        return strExtraData;
    }

    public void setStrExtraData(String strExtraData) {
        this.strExtraData = strExtraData;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public FactureDTO(Long id, String datePaiement, String montantPaiement, String referenceFacature, String referencePaiement, String produit, String strExtraData) {
        this.id = id;
        this.datePaiement = datePaiement;
        this.montantPaiement = montantPaiement;
        this.referenceFacature = referenceFacature;
        this.referencePaiement = referencePaiement;
        this.produit = produit;
        this.strExtraData=strExtraData;
        
    }

    public FactureDTO(Long id, String datePaiement, String montantPaiement, String referenceFacature, String referencePaiement,String referenceExt,String transactionIdExt, String produit,Long operationId,String strExtraData,String extraData) {
        this.id = id;
        this.datePaiement = datePaiement;
        this.montantPaiement = montantPaiement;
        this.referenceFacature = referenceFacature;
        this.referencePaiement = referencePaiement;
        this.referenceExt = referenceExt;
        this.transactionIdExt = transactionIdExt;
        this.produit = produit;
        this.operationId=operationId;
        this.strExtraData=strExtraData;
        this.extraData=extraData;
    }


    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", datePaiement='" + getDatePaiement() + "'" +
            ", montantPaiement='" + getMontantPaiement() + "'" +
            ", referenceFacature='" + getReferenceFacature() + "'" +
            ", referencePaiement='" + getReferencePaiement() + "'" +
            ", produit='" + getProduit() + "'" +
            "}";
    }


}
