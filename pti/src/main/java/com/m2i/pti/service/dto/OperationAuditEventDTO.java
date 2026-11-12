/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.ActionService;
import java.io.Serializable;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 *
 * @author cda
 */
public class OperationAuditEventDTO implements Serializable {

    private String actionService;
    private String referenceOperation;
    private String description;
    private String codeReponse;
    private String identifiant;
    
    
    
    
    
    
 private String typeAttribut;
    public String getActionService() {
        return actionService;
    }

    public void setActionService(String actionService) {
        this.actionService = actionService;
    }

    public String getReferenceOperation() {
        return referenceOperation;
    }

    public void setReferenceOperation(String referenceOperation) {
        this.referenceOperation = referenceOperation;
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
