/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "extra")
@XmlAccessorType(XmlAccessType.FIELD)
public class MParametre {
    @XmlElement(name = "codeParametre")
    private String codeParametre;
    @XmlElement(name = "valeur")
    private String valeur;
    @XmlElement(name = "affiche")
    private int affiche;

    public String getCodeParametre() {
        return codeParametre;
    }

    public void setCodeParametre(String codeParametre) {
        this.codeParametre = codeParametre;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public int getAffiche() {
        return affiche;
    }

    public void setAffiche(int affiche) {
        this.affiche = affiche;
    }

    public MParametre(String codeParametre, String valeur, int affiche) {
        this.codeParametre = codeParametre;
        this.valeur = valeur;
        this.affiche = affiche;
    }

    public MParametre() {
    }

}
