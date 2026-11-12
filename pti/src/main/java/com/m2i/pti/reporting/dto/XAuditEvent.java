package com.m2i.pti.reporting.dto;

public class XAuditEvent {
    private String action;
    private String entite;
    private String valeurEntite;
    private String dateEnregistrement;
    private String adresse;
    private String user;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntite() {
        return entite;
    }

    public void setEntite(String entite) {
        this.entite = entite;
    }

    public String getValeurEntite() {
        return valeurEntite;
    }

    public void setValeurEntite(String valeurEntite) {
        this.valeurEntite = valeurEntite;
    }

    public String getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(String dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
