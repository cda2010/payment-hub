/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain;

/**
 *
 * @author cda
 */
public class CSVTransfert extends CsvData {
    private String nom;
    private String prenom;
    private String numeroCompte;
    private Double  montant;
    private String numeroTelephone;

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public  void setNumeroCompte(String numeroCompte){ this.numeroCompte = numeroCompte ;}

    public String getNumeroCompte() {
        return numeroCompte;
    }
   
}
