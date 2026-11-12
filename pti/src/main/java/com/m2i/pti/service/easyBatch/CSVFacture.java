package com.m2i.pti.service.easyBatch;

public class CSVFacture extends CsvData {
    private String refAbonne;
    private String typePiece;
    private String numeroPiece;
    private String  soldePiece;
    private String dateLimitePiece;
    private String dateAppliPenalite;
    private String  dateLettrage;
    private String categorieComAbonne;
    private String  nomAbonne;
    private String anneFacture;
    private String  moisFacture;

    public String getRefAbonne() {
        return refAbonne;
    }

    public void setRefAbonne(String refAbonne) {
        this.refAbonne = refAbonne;
    }

    public String getTypePiece() {
        return typePiece;
    }

    public void setTypePiece(String typePiece) {
        this.typePiece = typePiece;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public String getSoldePiece() {
        return soldePiece;
    }

    public void setSoldePiece(String soldePiece) {
        this.soldePiece = soldePiece;
    }

    public String getDateLimitePiece() {
        return dateLimitePiece;
    }

    public void setDateLimitePiece(String dateLimitePiece) {
        this.dateLimitePiece = dateLimitePiece;
    }

    public String getDateAppliPenalite() {
        return dateAppliPenalite;
    }

    public void setDateAppliPenalite(String dateAppliPenalite) {
        this.dateAppliPenalite = dateAppliPenalite;
    }

    public String getDateLettrage() {
        return dateLettrage;
    }

    public void setDateLettrage(String dateLettrage) {
        this.dateLettrage = dateLettrage;
    }

    public String getCategorieComAbonne() {
        return categorieComAbonne;
    }

    public void setCategorieComAbonne(String categorieComAbonne) {
        this.categorieComAbonne = categorieComAbonne;
    }

    public String getNomAbonne() {
        return nomAbonne;
    }

    public void setNomAbonne(String nomAbonne) {
        this.nomAbonne = nomAbonne;
    }

    public String getAnneFacture() {
        return anneFacture;
    }

    public void setAnneFacture(String anneFacture) {
        this.anneFacture = anneFacture;
    }

    public String getMoisFacture() {
        return moisFacture;
    }

    public void setMoisFacture(String moisFacture) {
        this.moisFacture = moisFacture;
    }
}
