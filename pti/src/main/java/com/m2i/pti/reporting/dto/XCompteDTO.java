package com.m2i.pti.reporting.dto;

public class XCompteDTO {
    public String numeroCompte;
    public String numeroTel;
    public String etat;
    public String nomPrenom;
    public String dateCreation;
    public String dateDernierOp;
    public String duree;
    public String categorieCompte;
    public String libelleUniteOrganisation;
    public String codeUo;

    public String getDateDernierOp() {
        return dateDernierOp;
    }

    public void setDateDernierOp(String dateDernierOp) {
        this.dateDernierOp = dateDernierOp;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getEtat() {
        return etat;
    }

    public String getNumeroTel() {
        return numeroTel;
    }

    public void setNumeroTel(String numeroTel) {
        this.numeroTel = numeroTel;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getNomPrenom() {
        return nomPrenom;
    }

    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }

    public String getCategorieCompte() {
        return categorieCompte;
    }

    public void setCategorieCompte(String categorieCompte) {
        this.categorieCompte = categorieCompte;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }
}
