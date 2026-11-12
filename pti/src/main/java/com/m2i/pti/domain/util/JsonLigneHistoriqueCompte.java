/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.pti.domain.DetailHistorique;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author cda
 */
public class JsonLigneHistoriqueCompte implements  Serializable{
     @JsonProperty("listeCompte")
    private List<DetailHistorique> ligneDetailHistorique;

    public List<DetailHistorique> getLigneDetailHistorique() {
        return ligneDetailHistorique;
    }

    public void setLigneDetailHistorique(List<DetailHistorique> ligneDetailHistorique) {
        this.ligneDetailHistorique = ligneDetailHistorique;
    }
     
     
     
}
