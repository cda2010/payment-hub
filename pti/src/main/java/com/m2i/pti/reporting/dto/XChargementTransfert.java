package com.m2i.pti.reporting.dto;

import java.util.List;

public class XChargementTransfert {
    public String nomFichier;
    public Double montantTotal;
    public Integer nombreLigne;
    public Integer nombreLigneCharger;
    public Integer nombreLigneErreur;
    public String reference;
    public String lastModifiedBy;
    public String createBy;
    public String logo;
    public List<XligneCharger> ligneCharger;

    public String getNomFichier() {
        return this.nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public Double getMontantTotal() {
        return this.montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Integer getNombreLigne() {
        return this.nombreLigne;
    }

    public void setNombreLigne(Integer nombreLigne) {
        this.nombreLigne = nombreLigne;
    }

    public Integer getNombreLigneCharger() {
        return this.nombreLigneCharger;
    }

    public void setNombreLigneCharger(Integer nombreLigneCharger) {
        this.nombreLigneCharger = nombreLigneCharger;
    }

    public Integer getNombreLigneErreur() {
        return this.nombreLigneErreur;
    }

    public void setNombreLigneErreur(Integer nombreLigneErreur) {
        this.nombreLigneErreur = nombreLigneErreur;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getCreateBy() {
        return this.createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<XligneCharger> getLigneCharger() {
        return this.ligneCharger;
    }

    public void setLigneCharger(List<XligneCharger> ligneCharger) {
        this.ligneCharger = ligneCharger;
    }


}
