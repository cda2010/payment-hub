/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;


/**
 *
 * @author cda
 */
public class MFraisResponse extends MResponse{
    MFrais fees;

    public MFraisResponse() {
        super();
    }

    public MFrais getFees() {
        return fees;
    }

    public void setFees(MFrais fees) {
        this.fees = fees;
    }
    
}
