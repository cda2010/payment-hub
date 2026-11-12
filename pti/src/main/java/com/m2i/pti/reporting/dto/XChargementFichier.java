package com.m2i.pti.reporting.dto;

import java.util.List;

public class XChargementFichier {
    public String nomFichier;
    public Double montantTotal;
    public Integer nombreLigne;
    public Integer nombreLigneCharger;
    public Integer nombreLigneErreur;
    public String reference;
    public String lastModifiedBy;
    public String createBy;
    public String logo;
    public List<XLigneChargerFichier> ligneCharger;

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Integer getNombreLigne() {
        return nombreLigne;
    }

    public void setNombreLigne(Integer nombreLigne) {
        this.nombreLigne = nombreLigne;
    }

    public Integer getNombreLigneCharger() {
        return nombreLigneCharger;
    }

    public void setNombreLigneCharger(Integer nombreLigneCharger) {
        this.nombreLigneCharger = nombreLigneCharger;
    }

    public Integer getNombreLigneErreur() {
        return nombreLigneErreur;
    }

    public void setNombreLigneErreur(Integer nombreLigneErreur) {
        this.nombreLigneErreur = nombreLigneErreur;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<XLigneChargerFichier> getLigneCharger() {
        return ligneCharger;
    }

    public void setLigneCharger(List<XLigneChargerFichier> ligneCharger) {
        this.ligneCharger = ligneCharger;
    }
}
