/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.domain;

import com.m2i.pti.mobile.dto.MFrais;
import com.m2i.pti.mobile.dto.MResponse;

/**
 *
 * @author cda
 */
public class MFraisResponse extends MResponse{
    MFrais fees;

    public MFrais getFees() {
        return fees;
    }

    public void setFees(MFrais fees) {
        this.fees = fees;
    }
    
}
