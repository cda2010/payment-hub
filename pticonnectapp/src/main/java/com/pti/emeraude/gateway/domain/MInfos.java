package com.pti.emeraude.gateway.domain;

public class MInfos {
    MPersonne titulaire;
    MCompte compte;

    public MPersonne getTitulaire() {
        return titulaire;
    }

    public void setTitulaire(MPersonne titulaire) {
        this.titulaire = titulaire;
    }

    public MCompte getCompte() {
        return compte;
    }

    public void setCompte(MCompte compte) {
        this.compte = compte;
    }
}
