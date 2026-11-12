/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.util.List;

/**
 *
 * @author cda
 */
public class MListParametreProduit {
    private Long idProduit;
    List<MParametre> mparametre;

    public Long getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Long idProduit) {
        this.idProduit = idProduit;
    }

    public List<MParametre> getMparametre() {
        return mparametre;
    }

    public void setMparametre(List<MParametre> mparametre) {
        this.mparametre = mparametre;
    }
    
            
}
