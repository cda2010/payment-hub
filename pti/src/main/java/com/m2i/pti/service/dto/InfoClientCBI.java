package com.m2i.pti.service.dto;

import java.util.List;

public class InfoClientCBI {

    String nom;
    String prenom;
    String dateNaissance;
    String lieuNaissance;
    String sexe;

    private String piece;
    private String reference;
    private String dateDelivre;
    private String dateExpire;
    private String autoriteDelivre;
    private String lieuDelivre;
    private String telephone;
    private String codeIndicatif;
    private String typeClient;
    String numeroCompte;
    String agence;
    String cle;
    String codeClient;
    String titre;
    List<CompteCBI> listCompte;

    public String getDateExpire() {
        return dateExpire;
    }

    public void setDateExpire(String dateExpire) {
        this.dateExpire = dateExpire;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
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

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public List<CompteCBI> getListCompte() {
        return listCompte;
    }

    public void setListCompte(List<CompteCBI> listCompte) {
        this.listCompte = listCompte;
    }

    public String getPiece() {
        return piece;
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDateDelivre() {
        return dateDelivre;
    }

    public void setDateDelivre(String dateDelivre) {
        this.dateDelivre = dateDelivre;
    }

    public String getAutoriteDelivre() {
        return autoriteDelivre;
    }

    public void setAutoriteDelivre(String autoriteDelivre) {
        this.autoriteDelivre = autoriteDelivre;
    }

    public String getLieuDelivre() {
        return lieuDelivre;
    }

    public void setLieuDelivre(String lieuDelivre) {
        this.lieuDelivre = lieuDelivre;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }
    
    
}
