/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

/**
 *
 * @author cda
 */
public class ListFournisseurServiceDto {
    private String codeFse;
    private String libelleFse;
     String codeExterne;
    public String getCodeFse() {
        return codeFse;
    }

    public void setCodeFse(String codeFse) {
        this.codeFse = codeFse;
    }

    public String getLibelleFse() {
        return libelleFse;
    }

    public void setLibelleFse(String libelleFse) {
        this.libelleFse = libelleFse;
    }

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }
    
}
