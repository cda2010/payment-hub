package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.Champs;
import com.m2i.pti.domain.Ecran;
import com.m2i.pti.repository.ChampsRepository;

import java.io.Serializable;
import java.util.List;

public class MEcran implements Serializable {

    private Long id;

    private Integer numeroOrdre;
   private Integer produiId;
    private String produit;
    private List<MChamps> champs;
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
    
}
