/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.m2i.omnicanal.application.domain.util.JpaJsonDocumentsConverter;
import com.m2i.omnicanal.application.domain.util.JsonTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

/**
 *
 * @author cda
 */
public class Config {
    
    @Column(name = "configTransaction")
    @JsonProperty("configTransaction")
    @Convert (converter=JpaJsonDocumentsConverter.class)
    private JsonTransaction  configTransaction;
      @Column(name = "configPresentation")
    @JsonProperty("configPresentation")
    @Convert (converter=JpaJsonDocumentsConverter.class)
    private JsonTransaction  configPresentation;

    public JsonTransaction getConfigTransaction() {
        return configTransaction;
    }

    public void setConfigTransaction(JsonTransaction configTransaction) {
        this.configTransaction = configTransaction;
    }

    public JsonTransaction getConfigPresentation() {
        return configPresentation;
    }

    public void setConfigPresentation(JsonTransaction configPresentation) {
        this.configPresentation = configPresentation;
    }
      
}
