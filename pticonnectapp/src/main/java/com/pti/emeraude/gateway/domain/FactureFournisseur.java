/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cda
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class FactureFournisseur {
    @XmlElement(name = "reference")
    private String reference;
    @XmlElement(name = "description")
    private String description;
     @XmlElement(name = "montant")
     
     
    private String montant;
  @XmlElement(name = "extras")
    List<MParametre> extraParametres;
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public List<MParametre> getExtraParametres() {
        return extraParametres;
    }

    public void setExtraParametres(List<MParametre> extraParametres) {
        this.extraParametres = extraParametres;
    }
     
}
