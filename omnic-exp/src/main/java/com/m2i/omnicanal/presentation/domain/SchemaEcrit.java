/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import com.m2i.omnicanal.model.SchemaEcriture;


/**
 *
 * @author cda
 */
@Entity

public class SchemaEcrit extends SchemaEcriture{

    @Column(name = "pattern_code", length = 64, nullable = false)
    private String patternCode;

    public String getPatternCode() {
        return patternCode;
    }

    public void setPatternCode(String patternCode) {
        this.patternCode = patternCode;
    }
}
