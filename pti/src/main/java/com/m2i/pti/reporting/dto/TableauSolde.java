package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;

public class TableauSolde {
    Double distribution;
    Double client;
    Double master;
    Double agence;
    Double marchand;
    Double commission;
    Double taxe;
    Double commissionAtt;
    Double transfertAtt;
    Double plateforme;
    String logo;

    public Double getDistribution() {
        return distribution;
    }

    public void setDistribution(Double distribution) {
        this.distribution = distribution;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Double getClient() {
        return client;
    }

    public void setClient(Double client) {
        this.client = client;
    }

    public Double getMaster() {
        return master;
    }

    public void setMaster(Double master) {
        this.master = master;
    }

    public Double getAgence() {
        return agence;
    }

    public void setAgence(Double agence) {
        this.agence = agence;
    }

    public Double getMarchand() {
        return marchand;
    }

    public void setMarchand(Double marchand) {
        this.marchand = marchand;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public Double getTaxe() {
        return taxe;
    }

    public void setTaxe(Double taxe) {
        this.taxe = taxe;
    }

    public Double getCommissionAtt() {
        return commissionAtt;
    }

    public void setCommissionAtt(Double commissionAtt) {
        this.commissionAtt = commissionAtt;
    }

    public Double getTransfertAtt() {
        return transfertAtt;
    }

    public void setTransfertAtt(Double transfertAtt) {
        this.transfertAtt = transfertAtt;
    }

    public Double getPlateforme() {
        return plateforme;
    }

    public void setPlateforme(Double plateforme) {
        this.plateforme = plateforme;
    }
}
