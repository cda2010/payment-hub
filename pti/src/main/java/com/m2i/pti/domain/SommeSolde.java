package com.m2i.pti.domain;

import java.math.BigDecimal;

public class SommeSolde {
    BigDecimal solde;
    String libelle;

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }
}
