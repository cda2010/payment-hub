package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.ReportFormat;

import java.time.ZonedDateTime;

public class InfosFormulaire {

    private ReportFormat reportFormat;
    private boolean compteCloture;
    private boolean compteBloque;
    private String numTel;
    private String numCompte;
    private ZonedDateTime debut;
    private ZonedDateTime fin;

    public boolean isCompteCloture() {
        return compteCloture;
    }

    public void setCompteCloture(boolean compteCloture) {
        this.compteCloture = compteCloture;
    }

    public boolean isCompteBloque() {
        return compteBloque;
    }

    public void setCompteBloque(boolean compteBloque) {
        this.compteBloque = compteBloque;
    }

    public ReportFormat getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(ReportFormat reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public ZonedDateTime getDebut() {
        return debut;
    }

    public void setDebut(ZonedDateTime debut) {
        this.debut = debut;
    }

    public ZonedDateTime getFin() {
        return fin;
    }

    public void setFin(ZonedDateTime fin) {
        this.fin = fin;
    }
}
