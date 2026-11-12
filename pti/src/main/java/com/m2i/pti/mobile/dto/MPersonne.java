package com.m2i.pti.mobile.dto;

import com.m2i.omnicanal.outils.util.StandardResponse;
import com.m2i.pti.mobile.enumeration.MSexe;

import java.time.LocalDate;
import java.util.List;

public class MPersonne extends StandardResponse {
 private String codeNationalite;
    private String paysResidence;
    private String nom;
    private String prenom;
    private String telephone;
    private String codeTelephone;
    private String email;
    private String adresse;
    private String nomDelaMere;

    private String personneAprevenir;
    private MSexe sexe;
    private String lieuNaissance;
    private LocalDate dateNaissance;
    private String profession;
    private List<MDocIdentification> docIdentifications;
    private List<MDocument> imageDocumentIdentification;
 private MBrancheActivite brancheActivite;
    public List<MDocIdentification> getDocIdentifications() {
        return docIdentifications;
    }

    public void setDocIdentifications(List<MDocIdentification> docIdentifications) {
        this.docIdentifications = docIdentifications;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public MSexe getSexe() {
        return sexe;
    }

    public void setSexe(MSexe sexe) {
        this.sexe = sexe;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeTelephone() {
        return codeTelephone;
    }

    public void setCodeTelephone(String codeTelephone) {
        this.codeTelephone = codeTelephone;
    }

    public List<MDocument> getImageDocumentIdentification() {
        return imageDocumentIdentification;
    }

    public void setImageDocumentIdentification(List<MDocument> imageDocumentIdentification) {
        this.imageDocumentIdentification = imageDocumentIdentification;
    }

    public String getNomDelaMere() {
        return nomDelaMere;
    }

    public void setNomDelaMere(String nomDelaMere) {
        this.nomDelaMere = nomDelaMere;
    }

    public String getPersonneAprevenir() {
        return personneAprevenir;
    }

    public void setPersonneAprevenir(String personneAprevenir) {
        this.personneAprevenir = personneAprevenir;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCodeNationalite() {
        return codeNationalite;
    }

    public void setCodeNationalite(String codeNationalite) {
        this.codeNationalite = codeNationalite;
    }

    public String getPaysResidence() {
        return paysResidence;
    }

    public void setPaysResidence(String paysResidence) {
        this.paysResidence = paysResidence;
    }

    public MBrancheActivite getBrancheActivite() {
        return brancheActivite;
    }

    public void setBrancheActivite(MBrancheActivite brancheActivite) {
        this.brancheActivite = brancheActivite;
    }

}
