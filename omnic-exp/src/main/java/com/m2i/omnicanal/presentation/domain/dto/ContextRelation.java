/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.dto;

import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.enumeration.ImputeAgence;

import com.m2i.omnicanal.presentation.domain.FournisseurFSE;

/**
 *
 * @author cda
 */
public class ContextRelation {
     FournisseurService  fseSource;
     FournisseurService  fseDest;
     Agence agenceSource;
     Agence agenceDest;
     ImputeAgence agenceImput;

    public FournisseurService getFseSource() {
        return fseSource;
    }

    public void setFseSource(FournisseurService fseSource) {
        this.fseSource = fseSource;
    }

    public FournisseurService getFseDest() {
        return fseDest;
    }

    public void setFseDest(FournisseurService fseDest) {
        this.fseDest = fseDest;
    }

    public Agence getAgenceSource() {
        return agenceSource;
    }

    public void setAgenceSource(Agence agenceSource) {
        this.agenceSource = agenceSource;
    }

    public Agence getAgenceDest() {
        return agenceDest;
    }

    public void setAgenceDest(Agence agenceDest) {
        this.agenceDest = agenceDest;
    }

    public ImputeAgence getAgenceImput() {
        return agenceImput;
    }

    public void setAgenceImput(ImputeAgence agenceImput) {
        this.agenceImput = agenceImput;
    }
     
}
