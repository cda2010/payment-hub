package com.m2i.pti.reporting.dto;

import com.m2i.pti.reporting.util.ReportHeader;

public class XUser {
    private String nomPrenom;
    private String numTel;
    private String email;
    private String dateConnexion;
    private String etat;
    private String login;
    private String nomProfil;
    private String dateCreation;

    public String getNomPrenom() {
        return nomPrenom;
    }

    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateConnexion() {
        return dateConnexion;
    }

    public void setDateConnexion(String dateConnexion) {
        this.dateConnexion = dateConnexion;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNomProfil() {
        return nomProfil;
    }

    public void setNomProfil(String nomProfil) {
        this.nomProfil = nomProfil;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}
