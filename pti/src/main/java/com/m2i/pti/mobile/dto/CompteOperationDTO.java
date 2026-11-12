/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.Compte;

/**
 *
 * @author cda
 */
public class CompteOperationDTO {

    Compte compte;
    String compteExterne;
    String operationExterne;

    public CompteOperationDTO(Compte compte, String compteExterne, String operationExterne) {
        this.compte = compte;
        this.compteExterne = compteExterne;
        this.operationExterne = operationExterne;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getCompteExterne() {
        return compteExterne;
    }

    public void setCompteExterne(String compteExterne) {
        this.compteExterne = compteExterne;
    }

    public String getOperationExterne() {
        return operationExterne;
    }

    public void setOperationExterne(String operationExterne) {
        this.operationExterne = operationExterne;
    }

}
