/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.math.BigDecimal;

/**
 *
 * @author cda
 */
public class MRetraitResponse  extends StandardResponse{
    MPersonne mpersonne;
    MResponse reponse;
    BigDecimal montant;

    public MPersonne getMpersonne() {
        return mpersonne;
    }

    public void setMpersonne(MPersonne mpersonne) {
        this.mpersonne = mpersonne;
    }

    public MResponse getReponse() {
        return reponse;
    }

    public void setReponse(MResponse reponse) {
        this.reponse = reponse;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
}
