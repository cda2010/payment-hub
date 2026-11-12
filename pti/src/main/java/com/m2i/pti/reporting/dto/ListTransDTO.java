package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class ListTransDTO {
    public String intitulle;
    public String numeroTel;
    public Integer nbrTransaction;
    public String numeroCompte;
    public BigDecimal sommeDebit;
    public BigDecimal sommeCredit;
    public String getIntitulle() {
        return intitulle;
    }

    public void setIntitulle(String intitulle) {
        this.intitulle = intitulle;
    }


    public BigDecimal getSommeDebit() {
        return sommeDebit;
    }

    public void setSommeDebit(BigDecimal sommeDebit) {
        this.sommeDebit = sommeDebit;
    }

    public BigDecimal getSommeCredit() {
        return sommeCredit;
    }

    public void setSommeCredit(BigDecimal sommeCredit) {
        this.sommeCredit = sommeCredit;
    }

    public Integer getNbrTransaction() {
        return nbrTransaction;
    }

    public void setNbrTransaction(Integer nbrTransaction) {
        this.nbrTransaction = nbrTransaction;
    }

    public String getNumeroTel() {
        return numeroTel;
    }

    public void setNumeroTel(String numeroTel) {
        this.numeroTel = numeroTel;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }





}
