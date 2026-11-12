/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import java.util.List;

/**
 *
 * @author cda
 */
public class ParametreAppelDTO {
      private String fse;
        private String codeMethode;
        private         List <ParametreUser> param;

    public String getFse() {
        return fse;
    }

    public void setFse(String fse) {
        this.fse = fse;
    }

    public String getCodeMethode() {
        return codeMethode;
    }

    public void setCodeMethode(String codeMethode) {
        this.codeMethode = codeMethode;
    }

    public List<ParametreUser> getParam() {
        return param;
    }

    public void setParam(List<ParametreUser> param) {
        this.param = param;
    }
        
}
