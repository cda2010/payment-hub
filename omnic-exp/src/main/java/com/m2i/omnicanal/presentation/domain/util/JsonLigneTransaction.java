/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.omnicanal.presentation.domain.model.LigneTransaction;


import java.io.Serializable;

/**
 *
 * @author cda
 */
public class JsonLigneTransaction  implements  Serializable{
     @JsonProperty("ligneTransaction")
 private LigneTransaction ligneTransation;

    public LigneTransaction getLigneTransation() {
        return ligneTransation;
    }

    public void setLigneTransation(LigneTransaction ligneTransation) {
        this.ligneTransation = ligneTransation;
    }
     
}
