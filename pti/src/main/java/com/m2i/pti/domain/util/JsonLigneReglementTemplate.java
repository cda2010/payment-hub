/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.domain.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.pti.domain.LigneReglementTemplate;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cda
 */
public class JsonLigneReglementTemplate  implements  Serializable{

    @JsonProperty("listeUo")
    private LigneReglementTemplate ligneReglementTemplate;
    public LigneReglementTemplate getLigneReglementTemplate() {
        return ligneReglementTemplate;
    }
    public void setLigneReglementTemplate(LigneReglementTemplate ligneReglementTemplate) {
        this.ligneReglementTemplate = ligneReglementTemplate;
    }

    public JsonLigneReglementTemplate() {
    }

}
