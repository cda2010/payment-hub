/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

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
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class CollectionFacture {
      @XmlElement(name = "Factures")
       List<FactureFournisseur> listeFacture;

    public List<FactureFournisseur> getListeFacture() {
        return listeFacture;
    }

    public void setListeFacture(List<FactureFournisseur> listeFacture) {
        this.listeFacture = listeFacture;
    }

 
       
}
