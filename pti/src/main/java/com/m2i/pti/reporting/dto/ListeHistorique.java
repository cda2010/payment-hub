package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class ListeHistorique {

    public String numeroCpt;
    public String libelle;
    public String description;
    public BigDecimal solde;
    private String telephone;
    public String logo;
    public Integer format;
    public String codeUo;
    public String typeUo;



    public String getNumeroCpt() {
        return numeroCpt;
    }

    public void setNumeroCpt(String numeroCpt) {
        this.numeroCpt = numeroCpt;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public String getTypeUo() {
        return typeUo;
    }

    public void setTypeUo(String typeUo) {
        this.typeUo = typeUo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
