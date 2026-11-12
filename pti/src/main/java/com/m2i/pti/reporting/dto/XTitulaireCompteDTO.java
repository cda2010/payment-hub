package com.m2i.pti.reporting.dto;

import java.time.LocalDate;

public class XTitulaireCompteDTO {

    public String numeroCompte;
    public String typeCompte;
    public String numeroTel;
    public String nomPrenom;
    public String sexe;
    public LocalDate dateNaissance;

    public String getNumeroCompte() {
        return numeroCompte;
    }
    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }
    public String getTypeCompte() {
        return typeCompte;
    }
    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }
    public String getNumeroTel() {
        return numeroTel;
    }
    public void setNumeroTel(String numeroTel) {
        this.numeroTel = numeroTel;
    }
    public String getNomPrenom() {
        return nomPrenom;
    }
    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }
    public String getSexe() {
        return sexe;
    }
    public void setSexe(String sexe) {
        this.sexe = sexe;
    }
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

}
