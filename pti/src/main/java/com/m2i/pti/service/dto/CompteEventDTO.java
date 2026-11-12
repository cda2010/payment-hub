/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import com.m2i.pti.domain.Compte;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author cda
 */
public class CompteEventDTO implements Serializable {

Long compteId;
    String referenceOperation;
    String sens;
BigDecimal montant;
BigDecimal soldeCompte;
    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

 

   

    public String getReferenceOperation() {
        return referenceOperation;
    }

    public void setReferenceOperation(String referenceOperation) {
        this.referenceOperation = referenceOperation;
    }

    public String getSens() {
        return sens;
    }

    public void setSens(String sens) {
        this.sens = sens;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getSoldeCompte() {
        return soldeCompte;
    }

    public void setSoldeCompte(BigDecimal soldeCompte) {
        this.soldeCompte = soldeCompte;
    }


}
