/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service.dto;

/**
 *
 * @author cda
 */
public class DataChartResponse {
    String label;
    Long valeur;

    public DataChartResponse(String label, Long valeur) {
        this.label = label;
        this.valeur = valeur;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getValeur() {
        return valeur;
    }

    public void setValeur(Long valeur) {
        this.valeur = valeur;
    }
    
}
