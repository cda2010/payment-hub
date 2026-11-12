package com.m2i.pti.reporting.dto;

public class XBanqueReportDTO {
    public String numeroCompteBanquaire;
    public String telephone;
    public String prenomClient;
    public String nomClient;
    public String statutCompte;
    public String codeAgence;
    public String codeBanque;
    public String rib;

    public XBanqueReportDTO() {
    }

    public XBanqueReportDTO(String numeroCompteBanquaire, String telephone, String prenomClient, String nomClient, String statutCompte, String codeAgence, String codeBanque, String rib) {
        this.numeroCompteBanquaire = numeroCompteBanquaire;
        this.telephone = telephone;
        this.prenomClient = prenomClient;
        this.nomClient = nomClient;
        this.statutCompte = statutCompte;
        this.codeAgence = codeAgence;
        this.codeBanque = codeBanque;
        this.rib = rib;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(String statutCompte) {
        this.statutCompte = statutCompte;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getNumeroCompteBanquaire() {
        return numeroCompteBanquaire;
    }

    public void setNumeroCompteBanquaire(String numeroCompteBanquaire) {
        this.numeroCompteBanquaire = numeroCompteBanquaire;
    }
}
