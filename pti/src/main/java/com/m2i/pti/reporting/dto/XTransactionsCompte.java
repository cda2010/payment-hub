package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

import java.util.List;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.LocalDate;


public class XTransactionsCompte extends ReportHeader {

    private List<XLigneTransaction> listLigneTransactions;
    private LocalDate dateGeneration;
    private String numeroCompte;
    private String periode;
    private String logoComo;
    private String client;
    private String telephone;
    
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<XLigneTransaction> getListLigneTransactions() {
        return this.listLigneTransactions;
    }

    public void setListLigneTransactions(List<XLigneTransaction> listLigneTransactions) {
        this.listLigneTransactions = listLigneTransactions;
    }

    public LocalDate getDateGeneration() {
        return this.dateGeneration;
    }

    public void setDateGeneration(LocalDate dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public String getNumeroCompte() {
        return this.numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getPeriode() {
        return this.periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }
    


    public String getLogoComo() {
        return this.logoComo;
    }

    public void setLogoComo(String logoComo) {
        this.logoComo = logoComo;
    }


    @Override
    public String toString() {
        return "{" +
            " listLigneTransactions='" + getListLigneTransactions() + "'" +
            ", dateGeneration='" + getDateGeneration() + "'" +
            ", numeroCompte='" + getNumeroCompte() + "'" +
            ", periode='" + getPeriode() + "'" +
            "}";
    }



}