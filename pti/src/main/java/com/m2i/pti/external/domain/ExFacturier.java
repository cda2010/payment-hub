/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.external.domain;

import java.io.Serializable;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author USER_M2I
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExFacturier implements Serializable {

    @XmlAttribute(name = "id", required = true)
    private Long id;
    private static final long serialVersionUID = 1L;
    @XmlElement(name = "codeuo")
    private String codeUo;
    @XmlElement(name = "nomfacturier")
    private String nomFacturier;
    @XmlElement(name = "codefacturier")
    private String codeFacturier;
    @XmlElement(name = "codeuoposseseur")
    private String codeUoPossesseur;
    private byte[] image;
    private String valeurContentType;

    public ExFacturier(Long id, String codeUo, String codeFacturier,String nomFacturier,  byte[] image, String valeurContentType) {
        this.id = id;
         this.codeUo = codeUo;
        this.nomFacturier = nomFacturier;
        this.codeFacturier = codeFacturier;
        this.image = image;
        this.valeurContentType = valeurContentType;
       
    }
    
   

    public ExFacturier() {
    }

    public String getNomFacturier() {
        return nomFacturier;
    }
    @XmlElement(name = "Produits")
    private List<ExProduit> produit;

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public String getCodeUoPossesseur() {
        return codeUoPossesseur;
    }

    public void setCodeUoPossesseur(String codeUoPossesseur) {
        this.codeUoPossesseur = codeUoPossesseur;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public List<ExProduit> getProduit() {
        return produit;
    }

    public void setProduit(List<ExProduit> produit) {
        this.produit = produit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

  

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

}
