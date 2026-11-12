/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.mobile.dto.MParametre;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

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
