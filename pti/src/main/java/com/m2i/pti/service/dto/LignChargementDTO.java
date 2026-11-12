/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

/**
 *
 * @author cda
 */
public class LignChargementDTO {

    private Long id;
    private String nomLigne;
    private String nom;
    private String prenom;
    private String numeroCompte;

    private String numeroTelephone;

    private Double montant;

    private Double soldeApres;

    private String narration;

    private Long compteId;

    private String error;

    private String statutLigne;

    private Long chargementTransfertId;

    public LignChargementDTO(Long id, String nomLigne, String nom, String prenom, String numeroCompte, String numeroTelephone, Double montant, Double soldeApres, String narration, Long compteId, String error, String statutLigne, Long chargementTransfertId) {
        this.id = id;
        this.nomLigne = nomLigne;
        this.nom = nom;
        this.prenom = prenom;
        this.numeroCompte = numeroCompte;
        this.numeroTelephone = numeroTelephone;
        this.montant = montant;
        this.soldeApres = soldeApres;
        this.narration = narration;
        this.compteId = compteId;
        this.error = error;
        this.statutLigne = statutLigne;
        this.chargementTransfertId = chargementTransfertId;
    }

    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
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

    public Long getChargementTransfertId() {
        return chargementTransfertId;
    }

    public void setChargementTransfertId(Long chargementTransfertId) {
        this.chargementTransfertId = chargementTransfertId;
    }

}
