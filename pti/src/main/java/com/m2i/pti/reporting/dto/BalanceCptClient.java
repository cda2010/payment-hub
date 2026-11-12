package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class BalanceCptClient {
    public String numeroCpt;
    private String libelle;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal soldeDebit;
    private BigDecimal soldeCredit;
    private String intitulle;
    private String fseProprietaire;
    private String etiquette;
    private String logo;
    public String fmt;

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getFseProprietaire() {
        return fseProprietaire;
    }

    public void setFseProprietaire(String fseProprietaire) {
        this.fseProprietaire = fseProprietaire;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getIntitulle() {
        return intitulle;
    }

    public void setIntitulle(String intitulle) {
        this.intitulle = intitulle;
    }

    public String getNumeroCpt() {
        return numeroCpt;
    }

    public void setNumeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebits) {
        totalDebit = totalDebits;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredits) {
        totalCredit = totalCredits;
    }

    public BigDecimal getSoldeDebit() {
        return soldeDebit;
    }

    public void setSoldeDebit(BigDecimal soldeDebits) {
        soldeDebit = soldeDebits;
    }

    public BigDecimal getSoldeCredit() {
        return soldeCredit;
    }

    public void setSoldeCredit(BigDecimal soldeCredits) {
        soldeCredit = soldeCredits;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }
}
