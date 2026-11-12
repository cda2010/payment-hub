/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

/**
 *
 * @author hp
 */
public class FseDTO {
    private String codeFse;
    private String libelleFse;
    private String codeExterne;

    public String getCodeExterne() {
        return codeExterne;
    }

    public void setCodeExterne(String codeExterne) {
        this.codeExterne = codeExterne;
    }

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



}
