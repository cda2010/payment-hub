package com.m2i.pti.reporting.dto;

public class Bordereau {
    private String beneficiaire;
    private String telephoneB;
    private String typeB;
    private String noPieceB;
    private String noCompteB;

    private String paysOrganisationB;
    private String expediteur;
    private String telephoneE;
    private String typeE;
    private String noPieceE;
    private String noCompteE;

    private String organisation;
    private String pointVente;
    private String pays;
    private String paysDestination;
    private String montant;
    private String total;
    private String frais;
    private String taxe;
    private String date;
    private String titre;
    private String logo;
    private String codeOp;
    private String codeRetrait;

    public String getCodeOp() {
        return codeOp;
    }

    public void setCodeOp(String codeOp) {
        this.codeOp = codeOp;
    }

    public String getCodeRetrait() {
        return codeRetrait;
    }

    public void setCodeRetrait(String codeRetrait) {
        this.codeRetrait = codeRetrait;
    }

    public String getPaysOrganisationB() {
        return paysOrganisationB;
    }

    public void setPaysOrganisationB(String paysOrganisationB) {
        this.paysOrganisationB = paysOrganisationB;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public void setTelephoneB(String telephoneB) {
        this.telephoneB = telephoneB;
    }

    public void setTypeB(String typeB) {
        this.typeB = typeB;
    }

    public void setNoPieceB(String noPieceB) {
        this.noPieceB = noPieceB;
    }

    public String getNoCompteB() {
        return noCompteB;
    }

    public void setNoCompteB(String noCompteB) {
        this.noCompteB = noCompteB;
    }

    public void setExpediteur(String expediteur) {
        this.expediteur = expediteur;
    }

    public void setTelephoneE(String telephoneE) {
        this.telephoneE = telephoneE;
    }

    public void setTypeE(String typeE) {
        this.typeE = typeE;
    }

    public void setNoPieceE(String noPieceE) {
        this.noPieceE = noPieceE;
    }

    public String getNoCompteE() {
        return noCompteE;
    }

    public void setNoCompteE(String noCompteE) {
        this.noCompteE = noCompteE;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Bordereau logo(String logo) {
        this.logo = logo;
        return this;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setFrais(String frais) {
        this.frais = frais;
    }

    public void setTaxe(String taxe) {
        this.taxe = taxe;
    }

    public Bordereau beneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
        return this;
    }

    public String getPointVente() {
        return pointVente;
    }

    public void setPointVente(String pointVente) {
        this.pointVente = pointVente;
    }

    public String getPaysDestination() {
        return paysDestination;
    }

    public void setPaysDestination(String paysDestination) {
        this.paysDestination = paysDestination;
    }

    public String getTaxe() {
        return taxe;
    }

    public String getTelephoneB() {
        return telephoneB;
    }

    public Bordereau telephoneB(String telephoneB) {
        this.telephoneB = telephoneB;
        return this;
    }

    public String getTypeB() {
        return typeB;
    }

    public Bordereau typeB(String typeB) {
        this.typeB = typeB;
        return this;
    }



    public String getNoPieceB() {
        return noPieceB;
    }

    public Bordereau noPieceB(String noPieceB) {
        this.noPieceB = noPieceB;
        return this;
    }

    public String getExpediteur() {
        return expediteur;
    }

    public Bordereau expediteur(String expediteur) {
        this.expediteur = expediteur;
        return this;
    }

    public String getTelephoneE() {
        return telephoneE;
    }

    public Bordereau telephoneE(String telephoneE) {
        this.telephoneE = telephoneE;
        return this;
    }

    public String getTypeE() {
        return typeE;
    }

    public Bordereau typeE(String typeE) {
        this.typeE = typeE;
        return this;
    }



    public String getNoPieceE() {
        return noPieceE;
    }

    public Bordereau noPieceE(String noPieceE) {
        this.noPieceE = noPieceE;
        return this;
    }

    public String getOrganisation() {
        return organisation;
    }

    public Bordereau setOrganisation(String organisation) {
        this.organisation = organisation;
        return this;
    }

    public String getPays() {
        return pays;
    }

    public Bordereau pays(String pays) {
        this.pays = pays;
        return this;
    }

    public String getMontant() {
        return montant;
    }

    public String getFrais() {
        return frais;
    }

    public String getDate() {
        return date;
    }

    public Bordereau date(String date) {
        this.date = date;
        return this;
    }
}
