/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.pti.emeraude.gateway.domain.MParametre;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cda
 */
@XmlRootElement(name = "COMMAND")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(String.class)
public class CustomerInformation {

    @XmlElement(name = "codeAgence")
    String agence;
    @XmlElement(name = "codeClient")
    String codeClient;
    @XmlElement(name = "nom")
    String nom;
    @XmlElement(name = "prenom")
    String prenom;
    @XmlElement(name = "sexe")
    String sexe;
    @XmlElement(name = "titre")
    String titre;
    @XmlElement(name = "dateNaissance")
    String dateNaissance;
    @XmlElement(name = "lieuNaissance")
    String lieuNaissance;
    @XmlElement(name = "piece")
    private String piece;
    @XmlElement(name = "reference")
    private String reference;
    @XmlElement(name = "dateDelivre")
    private String dateDelivre;
    @XmlElement(name = "autoriteDelivre")
    private String autoriteDelivre;
    @XmlElement(name = "lieuDelivre")
    private String lieuDelivre;
    @XmlElement(name = "typeClient")
    private String typeClient;
    @XmlElement(name = "dateExpire")
    private String dateExpire;
    List<CustomerAccount> listCompte;

    @XmlElement(name = "extraParametres")

    List<MParametre> extraParametres;
  @XmlElement(name = "telephone")
    String telephone;
    @XmlElement(name = "codeIndicatif")
    String codeIndicatif;
     @XmlElement(name = "email")
    String email;
         @XmlElement(name = "adresse")
    String adresse;
            @XmlElement(name = "paysResidence")
    String paysResidence;
                @XmlElement(name = "nationalite")
    String nationalite;
    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
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

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
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

    public String getDateExpire() {
        return dateExpire;
    }

    public void setDateExpire(String dateExpire) {
        this.dateExpire = dateExpire;
    }

    public List<CustomerAccount> getListCompte() {
        return listCompte;
    }

    public void setListCompte(List<CustomerAccount> listCompte) {
        this.listCompte = listCompte;
    }

    public List<MParametre> getExtraParametres() {
        return extraParametres;
    }

    public void setExtraParametres(List<MParametre> extraParametres) {
        this.extraParametres = extraParametres;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeIndicatif() {
        return codeIndicatif;
    }

    public void setCodeIndicatif(String codeIndicatif) {
        this.codeIndicatif = codeIndicatif;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPaysResidence() {
        return paysResidence;
    }

    public void setPaysResidence(String paysResidence) {
        this.paysResidence = paysResidence;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }
    
    
}
