/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author cda
 */
public class ActionEventDTO {

    private String entityId;

    private String login;

    private String objet;

    private String action;

    private LocalDateTime dateAction;

    public ActionEventDTO() {
    }

    public ActionEventDTO(String entityId, String login, String objet, String action) {
        this.entityId = entityId;
        this.login = login;
        this.objet = objet;
        this.action = action;
    }
    public ActionEventDTO(String entityId, String login, String objet, String action, LocalDateTime dateAction) {
        this.entityId = entityId;
        this.login = login;
        this.objet = objet;
        this.action = action;
        this.dateAction=dateAction;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }
}
