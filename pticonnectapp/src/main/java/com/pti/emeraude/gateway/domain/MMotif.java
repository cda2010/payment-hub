package com.pti.emeraude.gateway.domain;

public class MMotif {
    private String code;
    private String libelle;



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MMotif code(String code) {
        this.code = code;
        return this;
    }

    public String getLibelle() {
        return libelle;
    }

    public MMotif libelle(String libelle) {
        this.libelle = libelle;
        return this;
    }
}
