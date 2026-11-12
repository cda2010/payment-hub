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
public class ExtActeurTransfert {
    ExtBwCustomer emeteur;
    ExtBwCustomer beneficiaire;

    public ExtBwCustomer getEmeteur() {
        return emeteur;
    }

    public void setEmeteur(ExtBwCustomer emeteur) {
        this.emeteur = emeteur;
    }

    public ExtBwCustomer getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(ExtBwCustomer beneficiaire) {
        this.beneficiaire = beneficiaire;
    }
    
    
    
}
