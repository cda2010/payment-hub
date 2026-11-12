/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
public class MfactureMarchand {
    private String referenceFacture;
    private String montantFacture;
    private String datePaiement ;
    private String referencePaiement;
    private String donneExterne;
   private String refExterne;
    private String nomProduit;
    public String getReferenceFacture() {
        return referenceFacture;
    }

    public void setReferenceFacture(String referenceFacture) {
        this.referenceFacture = referenceFacture;
    }

    public String getMontantFacture() {
        return montantFacture;
    }

    public void setMontantFacture(String montantFacture) {
        this.montantFacture = montantFacture;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getReferencePaiement() {
        return referencePaiement;
    }

    public void setReferencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
    }

    public String getDonneExterne() {
        return donneExterne;
    }

    public void setDonneExterne(String donneExterne) {
        this.donneExterne = donneExterne;
    }

    public String getRefExterne() {
        return refExterne;
    }

    public void setRefExterne(String refExterne) {
        this.refExterne = refExterne;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }
    
    
}
