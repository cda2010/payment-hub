package com.m2i.pti.reporting.dto;

import java.util.List;

public class XLigneChrgTransf {
    public String nomFichier;
    public Double montantTotal;
    public Integer nombreLigne;
    public Integer nombreLigneCharger;
    public Integer nombreLigneErreur;
    public String reference;
    public String lastModifiedBy;
    public String createBy;
    public String logo;
    public List<XligneCharger> ligneNonCharger;
    public List<XligneCharger> ligneCharger;

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

    public List<XligneCharger> getLigneNonCharger() {
        return ligneNonCharger;
    }

    public void setLigneNonCharger(List<XligneCharger> ligneNonCharger) {
        this.ligneNonCharger = ligneNonCharger;
    }

    public List<XligneCharger> getLigneCharger() {
        return ligneCharger;
    }

    public void setLigneCharger(List<XligneCharger> ligneCharger) {
        this.ligneCharger = ligneCharger;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }


    @Override
    public String toString() {
        return "{" +
            " nomFichier='" + getNomFichier() + "'" +
            ", montantTotal='" + getMontantTotal() + "'" +
            ", nombreLigne='" + getNombreLigne() + "'" +
            ", nombreLigneCharger='" + getNombreLigneCharger() + "'" +
            ", nombreLigneErreur='" + getNombreLigneErreur() + "'" +
            ", reference='" + getReference() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", createBy='" + getCreateBy() + "'" +
            ", logo='" + getLogo() + "'" +
            ", ligneNonCharger='" + getLigneNonCharger() + "'" +
            ", ligneCharger='" + getLigneCharger() + "'" +
            "}";
    }

}
