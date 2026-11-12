/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import com.m2i.omnicanal.model.FournisseurService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 *
 * @author cda
 */
@Entity
public class FournisseurFSE extends FournisseurService  {

    @Column(name = "type_moyen", length = 30)
    private String typeMoyen;

    @Column(name = "mode_execution", length = 30)
    private String modeExecution;

    public String getTypeMoyen() {
        return typeMoyen;
    }

    public void setTypeMoyen(String typeMoyen) {
        this.typeMoyen = typeMoyen;
    }

    public String getModeExecution() {
        return modeExecution;
    }

    public void setModeExecution(String modeExecution) {
        this.modeExecution = modeExecution;
    }
}
