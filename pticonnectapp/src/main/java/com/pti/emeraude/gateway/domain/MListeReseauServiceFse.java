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
public class MListeReseauServiceFse {
     String codePays;
    List<MServiceReseau> listMServiceReseau;

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public List<MServiceReseau> getListMServiceReseau() {
        return listMServiceReseau;
    }

    public void setListMServiceReseau(List<MServiceReseau> listMServiceReseau) {
        this.listMServiceReseau = listMServiceReseau;
    }
    
    
}
