package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.Produit;

import java.io.Serializable;

public class MProduit implements Serializable {

    private Long id;

    private String nomProduit;

    private Integer numeroOrdre;

    private boolean actif;

    private String facturier;
    
 private boolean noScreen;
 private boolean transactionnel;
 private String serviceAssocie;
    public MProduit() {
    }

    private double montant;

    private boolean aUnMontant;

    public String getServiceAssocie() {
        return serviceAssocie;
    }

    public void setServiceAssocie(String serviceAssocie) {
        this.serviceAssocie = serviceAssocie;
    }


    public MProduit(Produit p){
        this.id=p.getId();
          if (p.getServiceAssocie()!=null)
        this.serviceAssocie=p.getServiceAssocie().getLibelle();
       
        this.nomProduit=p.getNomProduit();
        this.actif=p.getActif();
        this.numeroOrdre=p.getNumeroOrdre();
        this.facturier=p.getFacturier().getNomFacturier();
        this.aUnMontant=p.getUnMontant();
        this.montant=p.getMontant();
        this.noScreen=p.isNoScreen();
        this.transactionnel=p.isTransactionnel();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getFacturier() {
        return facturier;
    }

    public void setFacturier(String facturier) {
        this.facturier = facturier;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public boolean isaUnMontant() {
        return aUnMontant;
    }

    public void setaUnMontant(boolean aUnMontant) {
        this.aUnMontant = aUnMontant;
    }

    public boolean isNoScreen() {
        return noScreen;
    }

    public void setNoScreen(boolean noScreen) {
        this.noScreen = noScreen;
    }

    public boolean isTransactionnel() {
        return transactionnel;
    }

    public void setTransactionnel(boolean transactionnel) {
        this.transactionnel = transactionnel;
    }
    
    
}
