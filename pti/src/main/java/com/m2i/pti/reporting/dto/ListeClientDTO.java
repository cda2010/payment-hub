package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ListeClientDTO {
    public String nomClient;
    public String prenomClient;
    public String telephone;
    public String statut;
    public String profil;
    public LocalDate dateNaiss;
    public String nationalite;
    public String paysResidence;
    public BigDecimal solde;
    public String dirigent;
    private String siegeSocial;
    public Integer nbrTransaction;
    public String sexe;
    public String dateCreation;
    public String groupeProfilActeur;
    private String adresse;
    private Long id;


    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public LocalDate getDateNaiss() {
        return dateNaiss;
    }

    public void setDateNaiss(LocalDate dateNaiss) {
        this.dateNaiss = dateNaiss;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getPaysResidence() {
        return paysResidence;
    }

    public void setPaysResidence(String paysResidence) {
        this.paysResidence = paysResidence;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public String getDirigent() {
        return dirigent;
    }

    public void setDirigent(String dirigent) {
        this.dirigent = dirigent;
    }

    public String getSiegeSocial() {
        return siegeSocial;
    }

    public void setSiegeSocial(String siegeSocial) {
        this.siegeSocial = siegeSocial;
    }

    public Integer getNbrTransaction() {
        return nbrTransaction;
    }

    public void setNbrTransaction(Integer nbrTransaction) {
        this.nbrTransaction = nbrTransaction;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getGroupeProfilActeur() {
        return groupeProfilActeur;
    }

    public void setGroupeProfilActeur(String groupeProfilActeur) {
        this.groupeProfilActeur = groupeProfilActeur;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
