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
public class MServiceReseau {
    String code;
    
    List<MOrganisation> listOrganisation;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<MOrganisation> getListOrganisation() {
        return listOrganisation;
    }

    public void setListOrganisation(List<MOrganisation> listOrganisation) {
        this.listOrganisation = listOrganisation;
    }

    @Override
    public String toString() {
        return "MServiceReseau{" + "code=" + code + ", listOrganisation=" + listOrganisation + '}';
    }

  
    
}
