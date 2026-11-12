package com.pti.emeraude.gateway.domain;

import java.io.Serializable;

public class MProduit implements Serializable {

    private Long id;

    private String nomProduit;

    private Integer numeroOrdre;

    private Integer actif;

    private String facturier;

    private double montant;

    private boolean aUnMontant;
 private boolean noScreen;
 private boolean transactionnel;
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

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
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
