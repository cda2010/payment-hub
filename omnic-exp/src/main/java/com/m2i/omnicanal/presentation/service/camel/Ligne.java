/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;

/**
 *
 * @author cda
 */
public class Ligne {

    LigneTransaction lg;
    Long trnId;
    LigneOper ligneOper;
    TransactionDTO trans;

    public LigneTransaction getLg() {
        return lg;
    }

    public void setLg(LigneTransaction lg) {
        this.lg = lg;
    }

    public Long getTrnId() {
        return trnId;
    }

    public void setTrnId(Long trnId) {
        this.trnId = trnId;
    }

    public Ligne(LigneTransaction lg, Long trnId, LigneOper ligneOper, TransactionDTO trans) {
        this.lg = lg;
        this.trnId = trnId;
        this.ligneOper = ligneOper;
        this.trans = trans;
    }

    public TransactionDTO getTrans() {
        return trans;
    }

    public void setTrans(TransactionDTO trans) {
        this.trans = trans;
    }

    public LigneOper getLigneOper() {
        return ligneOper;
    }

    public void setLigneOper(LigneOper ligneOper) {
        this.ligneOper = ligneOper;
    }

}
