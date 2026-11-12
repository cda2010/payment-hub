package com.pti.emeraude.gateway.domain;


import java.io.Serializable;

public class MClassificationFacturier  {
    private Long id;

    private String libelleClassification;
    private Long niveauSuperieurId ;

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleClassification() {
        return libelleClassification;
    }

    public void setLibelleClassification(String libelleClassification) {
        this.libelleClassification = libelleClassification;
    }

 

    public Long getNiveauSuperieurId() {
        return niveauSuperieurId;
    }

    public void setNiveauSuperieurId(Long niveauSuperieurId) {
        this.niveauSuperieurId = niveauSuperieurId;
    }
}

