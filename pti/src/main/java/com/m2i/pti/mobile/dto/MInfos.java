package com.m2i.pti.mobile.dto;

public class MInfos {
  private   MPersonne titulaire;
  private   MCompte compte;

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
