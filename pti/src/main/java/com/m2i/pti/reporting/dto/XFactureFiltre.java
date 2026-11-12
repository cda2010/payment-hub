package com.m2i.pti.reporting.dto;

public class XFactureFiltre {
    String nomFacturier;
    String codeFacturier;
    Long classification_id;
    Long typeFacturier;
    Long possesseur_id;
    String format;

    public String getNomFacturier() {
        return nomFacturier;
    }

    public void setNomFacturier(String nomFacturier) {
        this.nomFacturier = nomFacturier;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public Long getClassification_id() {
        return classification_id;
    }

    public void setClassification_id(Long classification_id) {
        this.classification_id = classification_id;
    }

    public Long getTypeFacturier() {
        return typeFacturier;
    }

    public void setTypeFacturier(Long typeFacturier) {
        this.typeFacturier = typeFacturier;
    }

    public Long getPossesseur_id() {
        return possesseur_id;
    }

    public void setPossesseur_id(Long possesseur_id) {
        this.possesseur_id = possesseur_id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
