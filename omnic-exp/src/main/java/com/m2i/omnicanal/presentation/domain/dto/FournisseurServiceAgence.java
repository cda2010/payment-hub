/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.dto;

import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.model.FournisseurService;

/**
 *
 * @author cda
 */
public class FournisseurServiceAgence {
    FournisseurService fse;
    Agence agence;

    public FournisseurService getFse() {
        return fse;
    }

    public void setFse(FournisseurService fse) {
        this.fse = fse;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }
    
}
