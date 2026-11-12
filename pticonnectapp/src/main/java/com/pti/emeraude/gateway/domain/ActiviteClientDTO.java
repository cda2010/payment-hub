/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 *
 * @author cda
 */
public class ActiviteClientDTO {

    private Long id;
    private String statut;
    private String description;

    private BigDecimal montant;

    private String reference;

    private String codeFse;

    private ZonedDateTime dateOperation;

    public ActiviteClientDTO(String description,String reference,  ZonedDateTime dateOperation ,String statut,  BigDecimal montant) {
        this.statut = statut;
        this.description = description;
        this.montant = montant;
        this.reference = reference;
        this.dateOperation = dateOperation;
    }

    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(ZonedDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

 

}
