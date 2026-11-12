package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class XCommissionDTO {
    private String typeService;
    private String organisation;
    private String dateOp;
    private BigDecimal montant;
    private String acteur;
    private String estPaye;
    private String etat;

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getActeur() {
        return acteur;
    }


    public void setActeur(String acteur) {
        this.acteur = acteur;
    }

    public String getEstPaye() {
        return estPaye;
    }

    public void setEstPaye(String estPaye) {
        this.estPaye = estPaye;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getDateOp() {
        return dateOp;
    }

    public void setDateOp(String dateOp) {
        this.dateOp = dateOp;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
}
