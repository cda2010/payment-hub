/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.math.BigDecimal;

/**
 *
 * @author cda
 */
public class MCompteAgent {

    MCompte compteOperation;
    BigDecimal soldeOperation;
    MCompte compteCommission;
    BigDecimal soldeCommission;
    String nomOrganisation;

    public MCompte getCompteOperation() {
        return compteOperation;
    }

    public void setCompteOperation(MCompte compteOperation) {
        this.compteOperation = compteOperation;
    }

    public MCompte getCompteCommission() {
        return compteCommission;
    }

    public void setCompteCommission(MCompte compteCommission) {
        this.compteCommission = compteCommission;
    }

    public BigDecimal getSoldeOperation() {
        return soldeOperation;
    }

    public void setSoldeOperation(BigDecimal soldeOperation) {
        this.soldeOperation = soldeOperation;
    }

    public BigDecimal getSoldeCommission() {
        return soldeCommission;
    }

    public void setSoldeCommission(BigDecimal soldeCommission) {
        this.soldeCommission = soldeCommission;
    }

    public String getNomOrganisation() {
        return nomOrganisation;
    }

    public void setNomOrganisation(String nomOrganisation) {
        this.nomOrganisation = nomOrganisation;
    }

}
