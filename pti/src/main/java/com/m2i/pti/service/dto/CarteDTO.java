/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.math.BigDecimal;
import java.io.Serializable;
import jakarta.persistence.Column;

/**
 *
 * @author cda
 */
public class CarteDTO implements Serializable {
    private Long id;
    private BigDecimal solde;
    private String codeFse;
    private String last4Digit;
    private String dateExpired;
    private String nomOnCard;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

  
   

    public String getLast4Digit() {
        return last4Digit;
    }

    public void setLast4Digit(String last4Digit) {
        this.last4Digit = last4Digit;
    }

    public String getDateExpired() {
        return dateExpired;
    }

    public void setDateExpired(String dateExpired) {
        this.dateExpired = dateExpired;
    }

    public String getNomOnCard() {
        return nomOnCard;
    }

    public void setNomOnCard(String nomOnCard) {
        this.nomOnCard = nomOnCard;
    }

}
