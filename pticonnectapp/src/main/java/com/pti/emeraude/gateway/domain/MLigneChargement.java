/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
public class MLigneChargement {

    private Long ligneChargementTransfertId;

    private String nomLigne;

    private String nom;

    private String prenom;

    private String numeroCompte;

    private String numeroTelephone;

    private Double montant;

    private Double soldeApres;

    private String error;

    private String statutLigne;

    public Long getLigneChargementTransfertId() {
        return ligneChargementTransfertId;
    }

    public void setLigneChargementTransfertId(Long ligneChargementTransfertId) {
        this.ligneChargementTransfertId = ligneChargementTransfertId;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public void setNomLigne(String nomLigne) {
        this.nomLigne = nomLigne;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Double getSoldeApres() {
        return soldeApres;
    }

    public void setSoldeApres(Double soldeApres) {
        this.soldeApres = soldeApres;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatutLigne() {
        return statutLigne;
    }

    public void setStatutLigne(String statutLigne) {
        this.statutLigne = statutLigne;
    }

}
