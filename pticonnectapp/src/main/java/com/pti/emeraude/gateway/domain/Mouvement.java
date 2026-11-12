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
public class Mouvement {
    @XmlElement(name = "DateTrans")
    private String dateMvt;
    @XmlElement(name = "Montant")
    private Double montantMvt;
    @XmlElement(name = "Sens")
    private String sens;
    @XmlElement(name = "Description")
    private String description;

    private String currency;

    private String rrn;

    public String getDateMvt() {
        return dateMvt;
    }

    public void setDateMvt(String dateMvt) {
        this.dateMvt = dateMvt;
    }

    public Double getMontantMvt() {
        return montantMvt;
    }

    public void setMontantMvt(Double montantMvt) {
        this.montantMvt = montantMvt;
    }

    public String getSens() {
        return sens;
    }

    public void setSens(String sens) {
        this.sens = sens;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }
}
