/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.io.Serializable;

/**
 *
 * @author cda
 */
public class MGroupeUniteOrganisation implements Serializable {

    Long id;
    String code;
    String libelle;

    public MGroupeUniteOrganisation( String code, String libelle) {
    
        this.code = code;
        this.libelle = libelle;
    }

    public MGroupeUniteOrganisation() {
    }

  
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

}
