package com.m2i.pti.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class XSousDistributeurDTO {

    public String libelleUniteOrganisation;
    public String codeUO;
    public String telephone;
    public String pereUO;
    public LocalDate dateCreation;
    public String villeNom;
    public BigDecimal solde;
    public BigDecimal commission_sd;
    public BigDecimal commission_m;
    public String volume_depot;
    public BigDecimal valeur_depot;
    public String volume_retrait;
    public BigDecimal valeur_retrait;

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public String getCodeUO() {
        return codeUO;
    }

    public void setCodeUO(String codeUO) {
        this.codeUO = codeUO;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPereUO() {
        return pereUO;
    }

    public void setPereUO(String pereUO) {
        this.pereUO = pereUO;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getVilleNom() {
        return villeNom;
    }

    public void setVilleNom(String villeNom) {
        this.villeNom = villeNom;
    }



    public String getVolume_depot() {
        return volume_depot;
    }

    public void setVolume_depot(String volume_depot) {
        this.volume_depot = volume_depot;
    }

    public String getVolume_retrait() {
        return volume_retrait;
    }

    public void setVolume_retrait(String volume_retrait) {
        this.volume_retrait = volume_retrait;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public BigDecimal getCommission_sd() {
        return commission_sd;
    }

    public void setCommission_sd(BigDecimal commission_sd) {
        this.commission_sd = commission_sd;
    }

    public BigDecimal getCommission_m() {
        return commission_m;
    }

    public void setCommission_m(BigDecimal commission_m) {
        this.commission_m = commission_m;
    }

    public BigDecimal getValeur_depot() {
        return valeur_depot;
    }

    public void setValeur_depot(BigDecimal valeur_depot) {
        this.valeur_depot = valeur_depot;
    }

    public BigDecimal getValeur_retrait() {
        return valeur_retrait;
    }

    public void setValeur_retrait(BigDecimal valeur_retrait) {
        this.valeur_retrait = valeur_retrait;
    }
}
