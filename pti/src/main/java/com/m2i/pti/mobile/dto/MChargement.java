/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import jakarta.persistence.Column;

/**
 *
 * @author cda
 */
public class MChargement {

    private Long id;

    private Double montantTotal;

    private String compteDebit;
    /*
    0 transfert vers compte 
    1 transfert vers cash
     */

    private Integer typeTransfert;

    private String codePays;

    private String motifOperation;

    private String description;
    private Double montantEntrp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getCompteDebit() {
        return compteDebit;
    }

    public void setCompteDebit(String compteDebit) {
        this.compteDebit = compteDebit;
    }

    public Integer getTypeTransfert() {
        return typeTransfert;
    }

    public void setTypeTransfert(Integer typeTransfert) {
        this.typeTransfert = typeTransfert;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public String getMotifOperation() {
        return motifOperation;
    }

    public void setMotifOperation(String motifOperation) {
        this.motifOperation = motifOperation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMontantEntrp() {
        return montantEntrp;
    }

    public void setMontantEntrp(Double montantEntrp) {
        this.montantEntrp = montantEntrp;
    }

}
