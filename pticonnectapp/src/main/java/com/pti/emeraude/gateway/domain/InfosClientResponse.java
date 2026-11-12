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
public class InfosClientResponse {
    private MPersonne personne;
    private MCompte compte;
  

    // Getters et Setters
    public MPersonne getPersonne() {
        return personne;
    }

    public void setPersonne(MPersonne personne) {
        this.personne = personne;
    }

    public MCompte getCompte() {
        return compte;
    }

    public void setCompte(MCompte compte) {
        this.compte = compte;
    }

}
