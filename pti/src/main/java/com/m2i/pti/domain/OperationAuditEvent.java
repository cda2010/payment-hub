/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

import com.m2i.pti.domain.enumeration.ActionService;
import java.io.Serializable;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author cda
 */
@Entity
@Table(name = "operation_audit_event")
public class OperationAuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "action_service")
    private ActionService actionService;
    private String referenceOperation;
    @Column(name = "date_enregistrement")
    private ZonedDateTime dateEnregistrement;
    private String codeReponse;
    private String description;
    private String identifiant;
    private String typeAttribut;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public String getReferenceOperation() {
        return referenceOperation;
    }

    public void setReferenceOperation(String referenceOperation) {
        this.referenceOperation = referenceOperation;
    }

    public ZonedDateTime getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(ZonedDateTime dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public String getCodeReponse() {
        return codeReponse;
    }

    public void setCodeReponse(String codeReponse) {
        this.codeReponse = codeReponse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getTypeAttribut() {
        return typeAttribut;
    }

    public void setTypeAttribut(String typeAttribut) {
        this.typeAttribut = typeAttribut;
    }

}
