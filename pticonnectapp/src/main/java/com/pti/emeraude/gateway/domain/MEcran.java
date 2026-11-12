package com.pti.emeraude.gateway.domain;


import java.io.Serializable;
import java.util.List;

public class MEcran implements Serializable {

    private Long id;

    private Integer numeroOrdre;

    private String produit;
    private List<MChamps> champs;
     private Integer produiId;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(Integer numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public List<MChamps> getChamps() {
        return champs;
    }

    public void setChamps(List<MChamps> champs) {
        this.champs = champs;
    }

    public Integer getProduiId() {
        return produiId;
    }

    public void setProduiId(Integer produiId) {
        this.produiId = produiId;
    }

    @Override
    public String toString() {
        return "MEcran{" + "id=" + id + ", numeroOrdre=" + numeroOrdre + ", produit=" + produit + ", champs=" + champs + ", produiId=" + produiId + '}';
    }
    
}
