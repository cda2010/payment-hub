package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XCompteMonnaieElectro {
    private String nomClient;
    private String prenomClient;
    private String telephone;
    private String statutCompte;
    private String etatCompte;
    private String nationalite;
    private String valeurCompte;
    private String categorieCompte;
    private BigDecimal solde;
    // public String dirigent;
    // private String siegeSocial;
    private Integer nbrTransaction;
    // public String sexe;
    private String dateCreation;


    public String getNomClient() {
        return nomClient;
    }
    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }
    public String getPrenomClient() {
        return prenomClient;
    }
    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getNationalite() {
        return nationalite;
    }
    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }
    
    public String getValeurCompte() {
        return valeurCompte;
    }
    public void setValeurCompte(String valeurCompte) {
        this.valeurCompte = valeurCompte;
    }
    public BigDecimal getSolde() {
        return solde;
    }
    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }
    public Integer getNbrTransaction() {
        return nbrTransaction;
    }
    public void setNbrTransaction(Integer nbrTransaction) {
        this.nbrTransaction = nbrTransaction;
    }
    public String getDateCreation() {
        return dateCreation;
    }
    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
    public String getStatutCompte() {
        return statutCompte;
    }
    public void setStatutCompte(String statutCompte) {
        this.statutCompte = statutCompte;
    }
    public String getEtatCompte() {
        return etatCompte;
    }
    public void setEtatCompte(String etatCompte) {
        this.etatCompte = etatCompte;
    }
    public String getCategorieCompte() {
        return categorieCompte;
    }
    public void setCategorieCompte(String categorieCompte) {
        this.categorieCompte = categorieCompte;
    }
    
}
