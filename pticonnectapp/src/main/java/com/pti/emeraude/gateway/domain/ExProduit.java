/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pti.emeraude.gateway.domain;


import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author USER_M2I
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ExProduit implements Serializable {
  @XmlAttribute(name = "id", required = true)
    private Long id;
    @XmlElement(name = "nomProduit")
    private String nomProduit;
    @XmlAttribute(name = "numeroOrdre")
    private Integer numeroOrdre;
    @XmlAttribute(name = "actif")
    private boolean actif;
        @XmlAttribute(name = "facturier")
    private String facturier;
    @XmlElement(name = "Ecran")
    private List<MEcran> ecrans;

    public ExProduit() {
    }
@XmlElement(name = "montant")
    private double montant;
@XmlElement(name = "aUnMontant")
    private boolean aUnMontant;

  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getFacturier() {
        return facturier;
    }

    public void setFacturier(String facturier) {
        this.facturier = facturier;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public boolean isaUnMontant() {
        return aUnMontant;
    }

    public void setaUnMontant(boolean aUnMontant) {
        this.aUnMontant = aUnMontant;
    }

    public List<MEcran> getEcrans() {
        return ecrans;
    }

    public void setEcrans(List<MEcran> ecrans) {
        this.ecrans = ecrans;
    }

    @Override
    public String toString() {
        return "ExProduit{" + "id=" + id + ", nomProduit=" + nomProduit + ", numeroOrdre=" + numeroOrdre + ", actif=" + actif + ", facturier=" + facturier + ", ecrans=" + ecrans + ", montant=" + montant + ", aUnMontant=" + aUnMontant + '}';
    }

}
