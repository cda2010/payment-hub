/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "parametre")
@XmlAccessorType(XmlAccessType.FIELD)
public class MParametre {

    private String codeParametre;
    private String valeur;
    private int affiche;

    public MParametre() {
    }

    public MParametre(String codeParametre, String valeur) {
        this.codeParametre = codeParametre;
        this.valeur = valeur;
    }

    public MParametre(String codeParametre, String valeur, int affiche) {
        this.codeParametre = codeParametre;
        this.valeur = valeur;
        this.affiche = affiche;
    }

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

    @Override
    public String toString() {
        return "MParametre{" + "codeParametre=" + codeParametre + ", valeur=" + valeur + ", affiche=" + affiche + '}';
    }
    
    

}
