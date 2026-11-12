/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.domain;

import java.math.BigDecimal;

/**
 *
 * @author cda
 */
public class ExtCallData {

    ExtBwCustomer compteInfo;
    String codeFacturier;
    String codeUoSrc;
    String codeOperateur;
    String canal;
    String codePv;
    String typeService;
    BigDecimal montant;
  
    public ExtBwCustomer getCompteInfo() {
        return compteInfo;
    }

    public void setCompteInfo(ExtBwCustomer compteInfo) {
        this.compteInfo = compteInfo;
    }

    public String getCodeFacturier() {
        return codeFacturier;
    }

    public void setCodeFacturier(String codeFacturier) {
        this.codeFacturier = codeFacturier;
    }

    public String getCodeUoSrc() {
        return codeUoSrc;
    }

    public void setCodeUoSrc(String codeUoSrc) {
        this.codeUoSrc = codeUoSrc;
    }

   

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }

   

}
