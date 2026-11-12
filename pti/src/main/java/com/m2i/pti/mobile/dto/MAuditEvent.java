/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.time.Instant;

/**
 *
 * @author cda
 */
public class MAuditEvent {
    Object objetAvantModif;
    Object objetApresModif;
    String action;
    String modifiedBy;
  Instant  modifiedDate;

    public Object getObjetAvantModif() {
        return objetAvantModif;
    }

    public void setObjetAvantModif(Object objetAvantModif) {
        this.objetAvantModif = objetAvantModif;
    }

    public Object getObjetApresModif() {
        return objetApresModif;
    }

    public void setObjetApresModif(Object objetApresModif) {
        this.objetApresModif = objetApresModif;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
  
  
}
