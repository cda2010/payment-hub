package com.m2i.pti.reporting.dto;

public class XligneCharger {
    public String numCpt;
    public Double montant;
    public String erreur;
    public String nom;
    public  String prenom;
    public String numTel;
    public String typePersonne;
    private String codeOperation;
    private String description;
    public String getNumCpt() {
        return numCpt;
    }

    public void setNumCpt(String numCpt) {
        this.numCpt = numCpt;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getErreur() {
        return erreur;
    }

    public void setErreur(String erreur) {
        this.erreur = erreur;
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

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getTypePersonne() {
        return typePersonne;
    }

    public void setTypePersonne(String typePersonne) {
        this.typePersonne = typePersonne;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "{" +
            " numCpt='" + getNumCpt() + "'" +
            ", montant='" + getMontant() + "'" +
            ", erreur='" + getErreur() + "'" +
            ", nom='" + getNom() + "'" +
            ", prenom='" + getPrenom() + "'" +
            ", numTel='" + getNumTel() + "'" +
            ", typePersonne='" + getTypePersonne() + "'" +
            "}";
    }


}
