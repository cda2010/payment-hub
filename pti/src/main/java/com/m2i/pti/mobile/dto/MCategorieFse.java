/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.dto;

import java.util.List;

/**
 *
 * @author cda
 */
public class MCategorieFse {
    
    String code;
    String libelle;
    List<MListTypeServiceFse> listTypeService;

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

    public List<MListTypeServiceFse> getListTypeService() {
        return listTypeService;
    }

    public void setListTypeService(List<MListTypeServiceFse> listTypeService) {
        this.listTypeService = listTypeService;
    }

  

    @Override
    public String toString() {
        return "MCategorieFse{" + "code=" + code + ", libelle=" + libelle + ", listTypeService=" + listTypeService + '}';
    }
    
    
    
}
