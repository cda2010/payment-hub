/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cda
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class RegleFacturier {
     @XmlElement(name = "message")
    private String message;
    @XmlElement(name = "action")
    private String action;
     @XmlElement(name = "Facture")
       FactureFournisseur facture;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public FactureFournisseur getFacture() {
        return facture;
    }

    public void setFacture(FactureFournisseur facture) {
        this.facture = facture;
    }
     
     
    
}



