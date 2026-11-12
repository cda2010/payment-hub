/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;


/**
 *
 * @author cda
 */
public class MParametrePaiementFacture {
      MListParametreProduit listParametreProduit;
      ExtBwCustomer compteInfo;
  
    String codeOperateur;

    public MListParametreProduit getListParametreProduit() {
        return listParametreProduit;
    }

    public void setListParametreProduit(MListParametreProduit listParametreProduit) {
        this.listParametreProduit = listParametreProduit;
    }

    public ExtBwCustomer getCompteInfo() {
        return compteInfo;
    }

    public void setCompteInfo(ExtBwCustomer compteInfo) {
        this.compteInfo = compteInfo;
    }

    public String getCodeOperateur() {
        return codeOperateur;
    }

    public void setCodeOperateur(String codeOperateur) {
        this.codeOperateur = codeOperateur;
    }
    
    
    
}
