/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author cda
 */
public class MchargementTransfert {

    private Long idChargement;

    private String nomFichier;

    private LocalDate dateChargement;

    private String description;

    private String reference;

    private Integer nombreLigne;

    private Integer nombreLigneCharge;

    private Integer nombreLigneErreur;

    private String statut;

    private List<MLigneChargement> mLigneChargement;

    public Long getIdChargement() {
        return idChargement;
    }

    public void setIdChargement(Long idChargement) {
        this.idChargement = idChargement;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public LocalDate getDateChargement() {
        return dateChargement;
    }

    public void setDateChargement(LocalDate dateChargement) {
        this.dateChargement = dateChargement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getNombreLigne() {
        return nombreLigne;
    }

    public void setNombreLigne(Integer nombreLigne) {
        this.nombreLigne = nombreLigne;
    }

    public Integer getNombreLigneCharge() {
        return nombreLigneCharge;
    }

    public void setNombreLigneCharge(Integer nombreLigneCharge) {
        this.nombreLigneCharge = nombreLigneCharge;
    }

    public Integer getNombreLigneErreur() {
        return nombreLigneErreur;
    }

    public void setNombreLigneErreur(Integer nombreLigneErreur) {
        this.nombreLigneErreur = nombreLigneErreur;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<MLigneChargement> getmLigneChargement() {
        return mLigneChargement;
    }

    public void setmLigneChargement(List<MLigneChargement> mLigneChargement) {
        this.mLigneChargement = mLigneChargement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
