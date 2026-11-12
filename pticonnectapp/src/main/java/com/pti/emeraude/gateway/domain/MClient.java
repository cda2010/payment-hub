package com.pti.emeraude.gateway.domain;

public class MClient {

    private MPersonne personne;
    private MCompte compte;
    private MDocIdentification docIdentification;
    private String codeFse;
    private String codePv;
    private String profil;
    private Integer idtId;

    public MDocIdentification getDocIdentification() {
        return docIdentification;
    }

    public void setDocIdentification(MDocIdentification docIdentification) {
        this.docIdentification = docIdentification;
    }

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

    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public Integer getIdtId() {
        return idtId;
    }

    public void setIdtId(Integer idtId) {
        this.idtId = idtId;
    }

}
