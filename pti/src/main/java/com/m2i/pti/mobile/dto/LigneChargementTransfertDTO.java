/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import com.m2i.pti.domain.LigneChargementTransfert;
import java.util.List;

/**
 *
 * @author cda
 */
public class LigneChargementTransfertDTO {

    Long compteDebit;
    Long operId;
    MLigneChargement ligneChargementTransfert;

    public MLigneChargement getLigneChargementTransfert() {
        return ligneChargementTransfert;
    }
    public void setLigneChargementTransfert(MLigneChargement ligneChargementTransfert) {
        this.ligneChargementTransfert = ligneChargementTransfert;
    }
    public Long getCompteDebit() {
        return compteDebit;
    }
    public void setCompteDebit(Long compteDebit) {
        this.compteDebit = compteDebit;
    }
    public Long getOperId() {
        return operId;
    }

    public void setOperId(Long operId) {
        this.operId = operId;
    }
   
}
