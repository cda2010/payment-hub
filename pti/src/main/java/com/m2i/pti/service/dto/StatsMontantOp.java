package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class StatsMontantOp implements Serializable{
    private String mois;
    private BigDecimal montant;

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
}
