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
public class MListeCategorieFse {
    
    String codeUo;
    List<MCategorieFse> listCategorie;

    public String getCodeUo() {
        return codeUo;
    }

    public void setCodeUo(String codeUo) {
        this.codeUo = codeUo;
    }

    public List<MCategorieFse> getListCategorie() {
        return listCategorie;
    }

    public void setListCategorie(List<MCategorieFse> listCategorie) {
        this.listCategorie = listCategorie;
    }

    @Override
    public String toString() {
        return "MListeCategorieFse{" + "codeUo=" + codeUo + ", listCategorie=" + listCategorie + '}';
    }
    
    
    
}
