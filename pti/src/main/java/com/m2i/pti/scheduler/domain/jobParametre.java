package com.m2i.pti.scheduler.domain;

import java.io.Serializable;

public class jobParametre
        implements Serializable {

    private String cle;

    private String valeur;

    public String getValeur() {
        return this.valeur;
    }
    public void setValeur(String valeur) {
        this.valeur = valeur;
    }
    public String getCle() {
        return cle;
    }
    public void setCle(String cle) {
        this.cle = cle;
    }

}
