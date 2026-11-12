package com.m2i.pti.reporting.dto;

public class XUserControl {

    public Long id;
    public String action;
    public String login;
    public String typeActeur;
    public String createdBy;
    public String dateAction;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTypeActeur() {
        return typeActeur;
    }

    public void setTypeActeur(String typeActeur) {
        this.typeActeur = typeActeur;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDateAction() {
        return dateAction;
    }

    public void setDateAction(String dateAction) {
        this.dateAction = dateAction;
    }
}
