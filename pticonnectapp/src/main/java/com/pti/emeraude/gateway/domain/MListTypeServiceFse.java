/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

import java.util.List;

/**
 *
 * @author cda
 */
public class MListTypeServiceFse {
 
    String codeService;
    String libeleService;
    List<MtypeService> typeServiceFse;

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getLibeleService() {
        return libeleService;
    }

    public void setLibeleService(String libeleService) {
        this.libeleService = libeleService;
    }

    public List<MtypeService> getTypeServiceFse() {
        return typeServiceFse;
    }

    public void setTypeServiceFse(List<MtypeService> typeServiceFse) {
        this.typeServiceFse = typeServiceFse;
    }


      
      
      
}
