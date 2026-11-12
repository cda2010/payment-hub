/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.Operation;

/**
 *
 * @author cda
 */
public class ChargementReponse {
    Operation op;
    Compte compteCredit;

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
    }

    public Compte getCompteCredit() {
        return compteCredit;
    }

    public void setCompteCredit(Compte compteCredit) {
        this.compteCredit = compteCredit;
    }
    
}
