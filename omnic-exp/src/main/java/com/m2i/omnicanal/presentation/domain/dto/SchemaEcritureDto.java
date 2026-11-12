/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.dto;

import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.ParametreSchema;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cda
 */
public class SchemaEcritureDto {
    private  LigneSchemaEcriture ligneSchema; 
    private Set <ParametreSchema>   parametreSchema;

    public LigneSchemaEcriture getLigneSchema() {
        return ligneSchema;
    }

    public void setLigneSchema(LigneSchemaEcriture ligneSchema) {
        this.ligneSchema = ligneSchema;
    }

    public Set<ParametreSchema> getParametreSchema() {
        return parametreSchema;
    }

    public void setParametreSchema(Set<ParametreSchema> parametreSchema) {
        this.parametreSchema = parametreSchema;
    }
    
}
